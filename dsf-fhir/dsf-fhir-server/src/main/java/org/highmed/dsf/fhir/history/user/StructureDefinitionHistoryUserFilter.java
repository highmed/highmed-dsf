package org.highmed.dsf.fhir.history.user;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.search.parameters.user.StructureDefinitionUserFilter;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public class StructureDefinitionHistoryUserFilter extends StructureDefinitionUserFilter implements HistoryUserFilter
{
	private static final String RESOURCE_TYPE = StructureDefinition.class.getAnnotation(ResourceDef.class).name();

	public StructureDefinitionHistoryUserFilter(User user)
	{
		super(user, HistoryUserFilter.RESOURCE_TABLE, HistoryUserFilter.RESOURCE_ID_COLUMN);
	}

	@Override
	public String getFilterQuery()
	{
		return HistoryUserFilter.getFilterQuery(RESOURCE_TYPE, super.getFilterQuery());
	}
}
