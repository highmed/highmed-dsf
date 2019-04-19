package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.StructureDefinitionDao;
import org.highmed.fhir.dao.StructureDefinitionSnapshotDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.SnapshotDependencyAnalyzer;
import org.highmed.fhir.service.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.StructureDefinition;

public class UpdateStructureDefinitionCommand extends UpdateCommand<StructureDefinition, StructureDefinitionDao>
		implements Command
{
	public UpdateStructureDefinitionCommand(int index, Bundle bundle, BundleEntryComponent entry,
			StructureDefinition resource, String serverBase, StructureDefinitionDao structureDefinitionDao,
			ReferenceReplacer replacer, ResponseGenerator responseGenerator, ExceptionHandler exceptionHandler,
			EventManager eventManager, EventGenerator eventGenerator,
			StructureDefinitionSnapshotDao structureDefinitionSnapshotDao, SnapshotGenerator snapshotGenerator,
			SnapshotDependencyAnalyzer snapshotDependencyAnalyzer, ParameterConverter parameterConverter)
	{
		super(index, bundle, entry, resource, serverBase, structureDefinitionDao, replacer, responseGenerator,
				exceptionHandler, eventManager, eventGenerator);
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preExecute(Connection connection) throws SQLException, WebApplicationException
	{
		// TODO Auto-generated method stub

		super.preExecute(connection);
	}

	@Override
	public BundleEntryComponent postExecute(Connection connection) throws SQLException, WebApplicationException
	{
		// TODO Auto-generated method stub
		
		return super.postExecute(connection);
	}
}
