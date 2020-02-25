package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class StructureDefinitionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public StructureDefinitionUserFilter(User user)
	{
		super(user, "structure_definition");
	}
}
