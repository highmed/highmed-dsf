package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractStatusParameter;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = ActivityDefinitionStatus.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/ActivityDefinition-status", type = SearchParamType.TOKEN, documentation = "The current status of the activity definition")
public class ActivityDefinitionStatus extends AbstractStatusParameter<ActivityDefinition>
{
	public ActivityDefinitionStatus()
	{
		super("activity_definition", ActivityDefinition.class);
	}
}
