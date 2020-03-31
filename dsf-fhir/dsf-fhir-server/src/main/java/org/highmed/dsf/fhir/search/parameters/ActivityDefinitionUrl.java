package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractUrlAndVersionParameter;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;

@SearchParameterDefinition(name = ActivityDefinitionUrl.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/ActivityDefinition-url", type = SearchParamType.URI, documentation = "The uri that identifies the activity definition")
public class ActivityDefinitionUrl extends AbstractUrlAndVersionParameter<ActivityDefinition>
{
	public static final String RESOURCE_COLUMN = "activity_definition";

	public ActivityDefinitionUrl()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	protected boolean instanceOf(Resource resource)
	{
		return resource instanceof ActivityDefinition;
	}
}
