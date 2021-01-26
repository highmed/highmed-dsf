package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.SnapshotGenerator;
import org.highmed.dsf.fhir.validation.SnapshotGenerator.SnapshotWithValidationMessages;
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

	private StructureDefinition resourceWithSnapshot;

	public CreateStructureDefinitionCommand(int index, User user, PreferReturnType returnType, Bundle bundle,
			BundleEntryComponent entry, String serverBase, AuthorizationHelper authorizationHelper,
			StructureDefinition resource, StructureDefinitionDao dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, ResponseGenerator responseGenerator,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, EventGenerator eventGenerator, StructureDefinitionDao snapshotDao)
	{
		super(index, user, returnType, bundle, entry, serverBase, authorizationHelper, resource, dao, exceptionHandler,
				parameterConverter, responseGenerator, referenceExtractor, referenceResolver, referenceCleaner,
				eventGenerator);

		this.snapshotDao = snapshotDao;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable, Connection connection,
			ValidationHelper validationHelper, SnapshotGenerator snapshotGenerator)
	{
		resourceWithSnapshot = resource.hasSnapshot() ? resource.copy()
				: generateSnapshot(snapshotGenerator, resource.copy());
		resource.setSnapshot(null);

		super.preExecute(idTranslationTable, connection, validationHelper, snapshotGenerator);
	}

	private StructureDefinition generateSnapshot(SnapshotGenerator snapshotGenerator, StructureDefinition resource)
	{
		logger.debug("Generating snapshot for bundle entry at index {}", index);
		SnapshotWithValidationMessages s = snapshotGenerator.generateSnapshot(resource);

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
