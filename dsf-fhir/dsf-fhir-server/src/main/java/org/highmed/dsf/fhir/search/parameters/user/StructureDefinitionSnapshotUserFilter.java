package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class StructureDefinitionSnapshotUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	public StructureDefinitionSnapshotUserFilter(User user)
	{
		super(user, "structure_definition_snapshot");
	}
}
