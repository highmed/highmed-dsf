package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class StructureDefinitionUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public StructureDefinitionUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "structure_definition");
	}
}
