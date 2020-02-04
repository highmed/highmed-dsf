package org.highmed.dsf.fhir.search;

import java.util.List;

public interface SearchQueryRevIncludeParameterFactory
{
	void configure(List<String> revIncludeParameterValues);

	List<SearchQueryParameterError> getErrors();

	List<SearchQueryIncludeParameter> getRevIncludeParameters();
}
