package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.PatientUserFilter;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class PatientHistoryUserFilter extends PatientUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = Patient.class.getAnnotation(ResourceDef.class).name();

	public PatientHistoryUserFilter(User user)
	{
		super(user, HistoryUserFilter.RESOURCE_TABLE, HistoryUserFilter.RESOURCE_ID_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
