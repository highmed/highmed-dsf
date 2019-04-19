package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
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

public class UpdateCommand<R extends DomainResource, D extends DomainResourceDao<R>> extends AbstractCommand<R, D>
		implements Command
{
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
	public void preExecute(Connection connection) throws SQLException, WebApplicationException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(Connection connection) throws SQLException, WebApplicationException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public BundleEntryComponent postExecute(Connection connection) throws SQLException, WebApplicationException
	{
		eventManager.handleEvent(eventGenerator.newResourceCreatedEvent(updatedResource));

		// TODO Auto-generated method stub
		return null;
	}
}
