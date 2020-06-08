package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateStructureDefinitionCommand extends CreateCommand<StructureDefinition, StructureDefinitionDao>
{
	private static final Logger logger = LoggerFactory.getLogger(CreateStructureDefinitionCommand.class);

	private final StructureDefinitionDao snapshotDao;
	private final Function<Connection, SnapshotGenerator> snapshotGenerator;

	private StructureDefinition resourceWithSnapshot;

	public CreateStructureDefinitionCommand(int index, User user, Bundle bundle, BundleEntryComponent entry,
			String serverBase, AuthorizationHelper authorizationHelper, ValidationHelper validationHelper,
			StructureDefinition resource, StructureDefinitionDao dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, ResponseGenerator responseGenerator,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, EventManager eventManager, EventGenerator eventGenerator,
			StructureDefinitionDao snapshotDao, Function<Connection, SnapshotGenerator> snapshotGenerator)
	{
		super(index, user, bundle, entry, serverBase, authorizationHelper, validationHelper, resource, dao,
				exceptionHandler, parameterConverter, responseGenerator, referenceExtractor, referenceResolver,
				referenceCleaner, eventManager, eventGenerator);

		this.snapshotDao = snapshotDao;
		this.snapshotGenerator = snapshotGenerator;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable, Connection connection)
	{
		resourceWithSnapshot = resource.hasSnapshot() ? resource.copy() : generateSnapshot(connection, resource.copy());
		resource.setSnapshot(null);

		super.preExecute(idTranslationTable, connection);
	}

	private StructureDefinition generateSnapshot(Connection connection, StructureDefinition resource)
	{
		logger.debug("Generating snapshot for bundle entry at index {}", index);
		SnapshotWithValidationMessages s = snapshotGenerator.apply(connection).generateSnapshot(resource);

		if (s.getMessages().stream()
				.anyMatch(m -> IssueSeverity.FATAL.equals(m.getLevel()) || IssueSeverity.ERROR.equals(m.getLevel())))
		{
			throw new WebApplicationException(
					responseGenerator.unableToGenerateSnapshot(resource, index, s.getMessages()));
		}

		return s.getSnapshot();
	}

	@Override
	protected StructureDefinition createWithTransactionAndId(Connection connection, StructureDefinition resource,
			UUID uuid) throws SQLException
	{
		StructureDefinition created = super.createWithTransactionAndId(connection, resource, uuid);

		if (resourceWithSnapshot != null)
		{
			try
			{
				snapshotDao.createWithTransactionAndId(connection, resourceWithSnapshot, uuid);
			}
			catch (SQLException e)
			{
				logger.warn("Error while creating StructureDefinition snapshot: " + e.getMessage(), e);
				throw e;
			}
		}

		return created;
	}
}
