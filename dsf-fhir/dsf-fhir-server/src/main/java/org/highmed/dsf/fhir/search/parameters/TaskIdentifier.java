package org.highmed.dsf.fhir.search.parameters;

import org.highmed.dsf.fhir.search.SearchQueryParameter.SearchParameterDefinition;
import org.highmed.dsf.fhir.search.parameters.basic.AbstractIdentifierParameter;
import org.hl7.fhir.r4.model.Enumerations.SearchParamType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;

@SearchParameterDefinition(name = AbstractIdentifierParameter.PARAMETER_NAME, definition = "http://hl7.org/fhir/SearchParameter/Task-identifier", type = SearchParamType.TOKEN, documentation = "Search for a task instance by its business identifier")
public class TaskIdentifier extends AbstractIdentifierParameter<Task>
{
	public static final String RESOURCE_COLUMN = "task";

	public TaskIdentifier()
	{
		super(RESOURCE_COLUMN);
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		if (!(resource instanceof Task))
			return false;

		Task t = (Task) resource;

		return identifierMatches(t.getIdentifier());
	}
}
