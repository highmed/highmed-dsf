package org.highmed.dsf.fhir.search.parameters.rev.include;

import java.sql.Connection;

import org.highmed.dsf.fhir.search.IncludeParameterDefinition;
import org.highmed.dsf.fhir.search.IncludeParts;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Resource;

@IncludeParameterDefinition(resourceType = OrganizationAffiliation.class, parameterName = "primary-organization", targetResourceTypes = Organization.class)
public class OrganizationAffiliationPrimaryOrganizationRevInclude extends AbstractRevIncludeParameterFactory
{
	public OrganizationAffiliationPrimaryOrganizationRevInclude()
	{
		super("OrganizationAffiliation", "primary-organization", "Organization");
	}

	@Override
	protected String getRevIncludeSql(IncludeParts includeParts)
	{
		return "(SELECT jsonb_agg(organization_affiliation) FROM current_organization_affiliations WHERE organization_affiliation->'organization' @> concat('{\"reference\": \"Organization/', organization->>'id', '\"}')::jsonb) AS organization_affiliations";
	}

	@Override
	protected void modifyIncludeResource(Resource resource, Connection connection)
	{
		// Nothing to do for organizations
	}
}
