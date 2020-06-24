package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.TaskUserFilter;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class TaskHistoryUserFilter extends TaskUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = Task.class.getAnnotation(ResourceDef.class).name();

	public TaskHistoryUserFilter(User user)
	{
		super(user, HistoryUserFilter.RESOURCE_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
