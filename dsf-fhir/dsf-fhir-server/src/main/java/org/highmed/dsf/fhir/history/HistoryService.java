package org.highmed.dsf.fhir.history;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public interface HistoryService
{
	Bundle getHistory(UriInfo uri, HttpHeaders headers);

	Bundle getHistory(UriInfo uri, HttpHeaders headers, Class<? extends Resource> resource);

	Bundle getHistory(UriInfo uri, HttpHeaders headers, Class<? extends Resource> resource, String id);
}
