package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.util.Map;

import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.function.ConsumerWithSqlAndResourceNotFoundException;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.SnapshotDependencies;
import org.highmed.dsf.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.service.SnapshotInfo;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStructureDefinitionCommand extends UpdateCommand<StructureDefinition, StructureDefinitionDao>
		implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateStructureDefinitionCommand.class);

	private final StructureDefinitionSnapshotDao snapshotDao;
	private final SnapshotGenerator snapshotGenerator;
	private final SnapshotDependencyAnalyzer snapshotDependencyAnalyzer;

	private StructureDefinition resourceWithSnapshot;

	public UpdateStructureDefinitionCommand(int index, Bundle bundle, BundleEntryComponent entry, String serverBase,
			StructureDefinition resource, StructureDefinitionDao dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, ResponseGenerator responseGenerator, EventManager eventManager,
			EventGenerator eventGenerator, StructureDefinitionSnapshotDao snapshotDao,
			SnapshotGenerator snapshotGenerator, SnapshotDependencyAnalyzer snapshotDependencyAnalyzer)
	{
		super(index, bundle, entry, serverBase, resource, dao, exceptionHandler, parameterConverter, responseGenerator,
				eventManager, eventGenerator);

		this.snapshotDao = snapshotDao;
		this.snapshotGenerator = snapshotGenerator;
		this.snapshotDependencyAnalyzer = snapshotDependencyAnalyzer;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable)
	{
		resourceWithSnapshot = resource.hasSnapshot() ? resource.copy() : null;
		resource.setSnapshot(null);

		super.preExecute(idTranslationTable);
	}

	@Override
	public BundleEntryComponent postExecute(Connection connection)
	{
		if (resourceWithSnapshot != null)
		{
			handleSnapshot(connection, resourceWithSnapshot,
					info -> snapshotDao.updateWithTransaction(connection, resourceWithSnapshot, info));
		}
		else if (updatedResource != null)
		{
			try
			{
				SnapshotWithValidationMessages s = snapshotGenerator.generateSnapshot(updatedResource);

				if (s != null && s.getSnapshot() != null && s.getMessages().isEmpty())
					handleSnapshot(connection, s.getSnapshot(),
							info -> snapshotDao.updateWithTransaction(connection, s.getSnapshot(), info));
			}
			catch (Exception e)
			{
				logger.warn("Error while generating snapshot for StructureDefinition with id "
						+ updatedResource.getIdElement().getIdPart(), e);
			}
		}

		return super.postExecute(connection);
	}

	private void handleSnapshot(Connection connection, StructureDefinition snapshot,
			ConsumerWithSqlAndResourceNotFoundException<SnapshotInfo> dbOp)
	{
		SnapshotDependencies dependencies = snapshotDependencyAnalyzer.analyzeSnapshotDependencies(snapshot);

		exceptionHandler.catchAndLogSqlException(
				() -> snapshotDao.deleteAllByDependencyWithTransaction(connection, snapshot.getUrl()));

		exceptionHandler.catchAndLogSqlAndResourceNotFoundException(resource.getResourceType().name(),
				() -> dbOp.accept(new SnapshotInfo(dependencies)));
	}
}
