package org.highmed.fhir.search;

import org.highmed.fhir.dao.search.DbSearchParameter;
import org.highmed.fhir.webservice.search.WsSearchParameter;
import org.hl7.fhir.r4.model.DomainResource;

public interface SearchParameter<R extends DomainResource> extends DbSearchParameter, WsSearchParameter
{
	boolean matches(R resource);
}
