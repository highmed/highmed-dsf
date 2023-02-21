package org.highmed.dsf.fhir.webservice.specification;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StructureDefinition;

public interface StructureDefinitionService extends BasicResourceService<StructureDefinition>
{
	Response postSnapshotNew(String snapshotPath, Parameters parameters, UriInfo uri, HttpHeaders headers);

	Response getSnapshotNew(String snapshotPath, UriInfo uri, HttpHeaders headers);

	Response postSnapshotExisting(String snapshotPath, String id, UriInfo uri, HttpHeaders headers);

	Response getSnapshotExisting(String snapshotPath, String id, UriInfo uri, HttpHeaders headers);
}
