package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Task;

@SearchParameterDefinition(name = TaskAuthoredOn.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Task-authored-on", type = SearchParamType.DATE, documentation = "Search by creation date")
public class TaskAuthoredOn extends AbstractDateTimeParameter<Task>
{
	public static final String PARAMETER_NAME = "authored-on";

	public TaskAuthoredOn()
	{
		super(PARAMETER_NAME, "task->>'authoredOn'");
	}
}
