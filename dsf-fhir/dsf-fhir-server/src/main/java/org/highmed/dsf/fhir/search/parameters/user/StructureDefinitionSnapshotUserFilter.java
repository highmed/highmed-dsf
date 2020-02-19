package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.authentication.User;

public class StructureDefinitionSnapshotUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public StructureDefinitionSnapshotUserFilter(OrganizationType organizationType, User user)
	{
		super(organizationType, user, "structure_definition_snapshot");
	}
}
