package org.highmed.dsf.fhir.search.parameters.rev.include;

import java.sql.Connection;

import org.highmed.dsf.fhir.search.IncludeParts;
import org.hl7.fhir.r4.model.Resource;

public class EndpointOrganizationRevInclude extends AbstractRevIncludeParameterFactory
{
	public EndpointOrganizationRevInclude()
	{
		super("Endpoint", "organization", "Organization");
	}

	@Override
	protected String getRevIncludeSql(IncludeParts includeParts)
	{
		return "(SELECT jsonb_build_array(organization) FROM current_organizations WHERE concat('Organization/', organization->>'id') = endpoint->'managingOrganization'->>'reference') AS organizations";
	}

	@Override
	protected void modifyIncludeResource(Resource resource, Connection connection)
	{
		// Nothing to do for organizations
	}
}
