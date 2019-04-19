package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.DomainResourceDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;

public class ResolveReferencesCommand<R extends DomainResource, D extends DomainResourceDao<R>>
		extends AbstractCommand<R, D> implements Command
{

	public ResolveReferencesCommand(int index, Bundle bundle, BundleEntryComponent entry,
			R resource, String serverBase, D dao)
	{
		super(5, index, bundle, entry, resource, serverBase, dao);
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
		// TODO Auto-generated method stub
		return null;
	}

}
