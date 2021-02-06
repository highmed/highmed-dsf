package org.highmed.dsf.fhir.search.parameters.rev.include;

import java.sql.Connection;

import org.highmed.dsf.fhir.search.IncludeParameterDefinition;
import org.highmed.dsf.fhir.search.IncludeParts;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = Endpoint.class, parameterName = "organization", targetResourceTypes = Organization.class)
public class EndpointOrganizationRevInclude extends AbstractRevIncludeParameterFactory
{
	public EndpointOrganizationRevInclude()
	{
		super("Endpoint", "organization", "Organization");
	}

	@Override
	protected String getRevIncludeSql(IncludeParts includeParts)
	{
		return "(SELECT jsonb_build_array(endpoint) FROM current_endpoints WHERE endpoint->'managingOrganization'->>'reference' = concat('Organization/', organization->>'id')) AS endpoints";
	}

	@Override
	protected void modifyIncludeResource(Resource resource, Connection connection)
	{
		// Nothing to do for organizations
	}
}
