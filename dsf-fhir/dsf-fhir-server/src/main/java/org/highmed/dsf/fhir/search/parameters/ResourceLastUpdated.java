package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = ResourceLastUpdated.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Resource-lastUpdated", type = SearchParamType.DATE, documentation = "When the resource version last changed")
public class ResourceLastUpdated<R extends DomainResource> extends AbstractDateTimeParameter<R>
{
	public static final String PARAMETER_NAME = "_lastUpdated";

	public ResourceLastUpdated(String resourceColumn)
	{
		super(PARAMETER_NAME, resourceColumn + "->'meta'->>'lastUpdated'");
	}
}
