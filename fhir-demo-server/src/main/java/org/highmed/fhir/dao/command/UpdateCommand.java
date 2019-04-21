package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ResponseGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCommand<R extends DomainResource, D extends DomainResourceDao<R>> extends AbstractCommand<R, D>
		implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateCommand.class);

	protected final ReferenceReplacer replacer;
	protected final ResponseGenerator responseGenerator;
	protected final ExceptionHandler exceptionHandler;
	protected final EventManager eventManager;
	protected final EventGenerator eventGenerator;

	protected UUID id;
	protected R updatedResource;

	public UpdateCommand(int index, Bundle bundle, BundleEntryComponent entry, R resource, String serverBase, D dao,
			ReferenceReplacer replacer, ResponseGenerator responseGenerator, ExceptionHandler exceptionHandler,
			EventManager eventManager, EventGenerator eventGenerator)
	{
		super(3, index, bundle, entry, resource, serverBase, dao);

		this.replacer = replacer;
		this.responseGenerator = responseGenerator;
		this.exceptionHandler = exceptionHandler;
		this.eventManager = eventManager;
		this.eventGenerator = eventGenerator;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public BundleEntryComponent postExecute()
	{
		try
		{
			eventManager.handleEvent(eventGenerator.newResourceUpdatedEvent(updatedResource));
		}
		catch (Exception e)
		{
			logger.warn("Error while handling resource updated event", e);
		}

		// TODO Auto-generated method stub
		return null;
	}
}
