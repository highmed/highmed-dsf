package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class CodeSystemUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public CodeSystemUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "code_system");
	}
}
