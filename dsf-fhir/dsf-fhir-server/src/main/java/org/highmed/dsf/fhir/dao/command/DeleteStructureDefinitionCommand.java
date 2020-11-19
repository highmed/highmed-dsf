package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteStructureDefinitionCommand extends DeleteCommand
{
	private static final Logger logger = LoggerFactory.getLogger(DeleteStructureDefinitionCommand.class);

	private StructureDefinitionDao snapshotDao;

	public DeleteStructureDefinitionCommand(int index, User user, PreferReturnType returnType, Bundle bundle,
			BundleEntryComponent entry, String serverBase, AuthorizationHelper authorizationHelper,
			ResponseGenerator responseGenerator, DaoProvider daoProvider, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, EventGenerator eventGenerator)
	{
		super(index, user, returnType, bundle, entry, serverBase, authorizationHelper, responseGenerator, daoProvider,
				exceptionHandler, parameterConverter, eventGenerator);

		snapshotDao = daoProvider.getStructureDefinitionSnapshotDao();
	}

	@Override
	protected boolean deleteWithTransaction(ResourceDao<?> dao, Connection connection, UUID uuid)
			throws SQLException, ResourceNotFoundException
	{
		boolean deleted = super.deleteWithTransaction(dao, connection, uuid);

		try
		{
			snapshotDao.deleteWithTransaction(connection, uuid);
		}
		catch (SQLException | ResourceNotFoundException e)
		{
			logger.warn("Error while deleting StructureDefinition snaphost for id " + uuid.toString()
					+ ", exception will be ignored", e);
		}

		return deleted;
	}
}
