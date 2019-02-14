package org.highmed.fhir.search;

import org.highmed.fhir.dao.search.DbSearchParameter;
import org.highmed.fhir.webservice.search.WsSearchParameter;

public interface SearchParameter extends DbSearchParameter, WsSearchParameter
{
}
