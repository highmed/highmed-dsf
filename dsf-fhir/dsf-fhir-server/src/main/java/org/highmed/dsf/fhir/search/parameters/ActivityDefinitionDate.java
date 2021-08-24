package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;

@SearchParameterDefinition(name = ActivityDefinitionDate.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/ActivityDefinition-date", type = SearchParamType.DATE, documentation = "The activity definition publication date")
public class ActivityDefinitionDate extends AbstractDateTimeParameter<ActivityDefinition>
{
	public static final String PARAMETER_NAME = "date";

	public ActivityDefinitionDate()
	{
		super(PARAMETER_NAME, "activity_definition->>'date'");
	}
}
