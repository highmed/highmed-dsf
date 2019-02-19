package org.highmed.fhir.webservice.secure;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.webservice.specification.StructureDefinitionService;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;

public class StructureDefinitionServiceSecure extends
		AbstractServiceSecure<StructureDefinition, StructureDefinitionService> implements StructureDefinitionService
{
	public StructureDefinitionServiceSecure(StructureDefinitionService delegate)
	{
		super(delegate);
	}

	@Override
	public Response create(StructureDefinition resource, UriInfo uri)
	{
		// check logged in, check "local" user (local user only could be default)
		// check against existing profiles, no create if profile with same URL, version and status exists

		// TODO Auto-generated method stub
		return super.create(resource, uri);
	}

	@Override
	public Response update(String id, StructureDefinition resource, UriInfo uri)
	{
		// check logged in, check "local" user (local user only could be default)
		// check resource exists for given path id
		// check against existing profile (by path id), no update if profile has different URL, version or status, status change via create
		// check against existing profile (by path id), no update if status ACTIVE or RETIRED
		
		// TODO Auto-generated method stub
		return super.update(id, resource, uri);
	}
	
	@Override
	public Response delete(String id, UriInfo uri)
	{
		//check logger in, check "local" user (local user only could be default)
		
		// TODO Auto-generated method stub
		return super.delete(id, uri);
	}

	@Override
	public Response postSnapshotNew(String snapshotPath, String format, Parameters parameters, UriInfo uri)
	{
		return delegate.postSnapshotNew(snapshotPath, format, parameters, uri);
	}

	@Override
	public Response getSnapshotNew(String snapshotPath, String url, String format, UriInfo uri)
	{
		return delegate.getSnapshotNew(snapshotPath, url, format, uri);
	}

	@Override
	public Response postSnapshotExisting(String snapshotPath, String id, String format, UriInfo uri)
	{
		return delegate.postSnapshotExisting(snapshotPath, id, format, uri);
	}

	@Override
	public Response getSnapshotExisting(String snapshotPath, String id, String format, UriInfo uri)
	{
		return delegate.getSnapshotExisting(snapshotPath, id, format, uri);
	}
}
