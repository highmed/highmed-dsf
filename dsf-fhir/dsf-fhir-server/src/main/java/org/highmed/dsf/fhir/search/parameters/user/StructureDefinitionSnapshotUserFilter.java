package org.highmed.dsf.fhir.search.parameters.user;

import org.highmed.dsf.fhir.authentication.User;

public class StructureDefinitionSnapshotUserFilter extends AbstractMetaTagAuthorizationRoleUserFilter
{
	private static final String RESOURCE_COLUMN = "structure_definition_snapshot";

	public StructureDefinitionSnapshotUserFilter(User user)
	{
		super(user, RESOURCE_COLUMN);
	}

	public StructureDefinitionSnapshotUserFilter(User user, String resourceColumn)
	{
		super(user, resourceColumn);
	}
}
