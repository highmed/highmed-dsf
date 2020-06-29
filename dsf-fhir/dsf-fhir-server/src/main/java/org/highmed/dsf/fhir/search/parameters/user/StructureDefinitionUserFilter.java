package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class StructureDefinitionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "structure_definition";

	public StructureDefinitionUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public StructureDefinitionUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
