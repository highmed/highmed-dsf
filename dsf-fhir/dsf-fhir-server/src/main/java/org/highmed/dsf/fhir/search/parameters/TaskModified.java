package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractDateTimeParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Task;

@SearchParameterDefinition(name = TaskModified.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Task-modified", type = SearchParamType.DATE, documentation = "Search by last modification date")
public class TaskModified extends AbstractDateTimeParameter<Task>
{
	public static final String PARAMETER_NAME = "modified";

	public TaskModified()
	{
		super(PARAMETER_NAME, "task->>'lastModified'");
	}
}
