package org.highmed.dsf.fhir.webservice.secure;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.StructureDefinitionService;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructureDefinitionServiceSecure extends
		AbstractServiceSecure<StructureDefinition, StructureDefinitionService> implements StructureDefinitionService
{
	private static final Logger logger = LoggerFactory.getLogger(StructureDefinitionServiceSecure.class);

	public StructureDefinitionServiceSecure(StructureDefinitionService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}

	@Override
	public Response create(StructureDefinition resource, UriInfo uri, HttpHeaders headers)
	{
		// check logged in, check "local" user (local user only could be default)
		// check against existing profiles, no create if profile with same URL, version and status exists

		// TODO Auto-generated method stub
		return super.create(resource, uri, headers);
	}

	@Override
	public Response update(String id, StructureDefinition resource, UriInfo uri, HttpHeaders headers)
	{
		// check logged in, check "local" user (local user only could be default)
		// check resource exists for given path id
		// check against existing profile (by path id), no update if profile has different URL, version or status,
		// status change via create
		// check against existing profile (by path id), no update if status ACTIVE or RETIRED

		// TODO Auto-generated method stub
		return super.update(id, resource, uri, headers);
	}

	@Override
	public Response update(StructureDefinition resource, UriInfo uri, HttpHeaders headers)
	{
		// see update above

		// TODO Auto-generated method stub
		return super.update(resource, uri, headers);
	}

	@Override
	public Response delete(String id, UriInfo uri, HttpHeaders headers)
	{
		// check logger in, check "local" user (local user only could be default)

		// TODO Auto-generated method stub
		return super.delete(id, uri, headers);
	}

	@Override
	public Response postSnapshotNew(String snapshotPath, Parameters parameters, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.postSnapshotNew(snapshotPath, parameters, uri, headers);
	}

	@Override
	public Response getSnapshotNew(String snapshotPath, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.getSnapshotNew(snapshotPath, uri, headers);
	}

	@Override
	public Response postSnapshotExisting(String snapshotPath, String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.postSnapshotExisting(snapshotPath, id, uri, headers);
	}

	@Override
	public Response getSnapshotExisting(String snapshotPath, String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", provider.getCurrentUser().getName(),
				provider.getCurrentUser().getRole());

		return delegate.getSnapshotExisting(snapshotPath, id, uri, headers);
	}
}
