package org.highmed.fhir.dao;

import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.OrganizationName;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationDao extends AbstractDomainResourceDao<Organization>
{
	public OrganizationDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Organization.class, "organizations", "organization", "organization_id",
				OrganizationName::new);
	}

	@Override
	protected Organization copy(Organization resource)
	{
		return resource.copy();
	}

	public Optional<Organization> readByIdentifier(String system, String value)
	{
		/*
		 * SELECT organizations.organization FROM organizations,
		 * jsonb_array_elements(organizations.organization->'identifier') AS identifiers WHERE identifiers->>'value' =
		 * '666f6f206261722062617a' AND identifiers->>'system' =
		 * 'http://highmed.org/fhir/NamingSystem/certificate-thumbprint'
		 */
		// TODO
		return Optional.empty();
	}
}
