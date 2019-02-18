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
	
	public Response postSnapshotNew(String snapshotPath, String format, Parameters parameters, UriInfo uri)
	{
		return delegate.postSnapshotNew(snapshotPath, format, parameters, uri);
	}

	@Override
	public Response getSnapshotNew(String snapshotPath, String url, String format, UriInfo uri)
	{
		return delegate.getSnapshotNew(snapshotPath, url, format, uri);
	}
	
	public Response postSnapshotExisting(String snapshotPath, String id, String format, UriInfo uri)
	{
		return delegate.postSnapshotExisting(snapshotPath, id, format, uri);
	}
	
	public Response getSnapshotExisting(String snapshotPath, String id, String format, UriInfo uri)
	{
		return delegate.getSnapshotExisting(snapshotPath, id, format, uri);
	}
}
