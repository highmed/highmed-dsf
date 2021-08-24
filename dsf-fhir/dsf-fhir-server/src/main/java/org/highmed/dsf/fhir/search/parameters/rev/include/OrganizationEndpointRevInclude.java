package org.highmed.dsf.fhir.search.parameters.rev.include;

import java.sql.Connection;

import org.highmed.dsf.fhir.search.IncludeParameterDefinition;
import org.highmed.dsf.fhir.search.IncludeParts;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = Organization.class, parameterName = "endpoint", targetResourceTypes = Endpoint.class)
public class OrganizationEndpointRevInclude extends AbstractRevIncludeParameterFactory
{
	public OrganizationEndpointRevInclude()
	{
		super("Organization", "endpoint", "Endpoint");
	}

	@Override
	protected String getRevIncludeSql(IncludeParts includeParts)
	{
		return "(SELECT jsonb_agg(organization) FROM current_organizations WHERE organization->'endpoint' @> concat('[{\"reference\": \"Endpoint/', endpoint->>'id', '\"}]')::jsonb) AS organizations";
	}

	@Override
	protected void modifyIncludeResource(Resource resource, Connection connection)
	{
		// Nothing to do for organizations
	}
}
