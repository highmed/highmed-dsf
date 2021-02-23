package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationActive;
import org.highmed.dsf.fhir.search.parameters.OrganizationAffiliationIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.OrganizationAffiliationUserFilter;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationAffiliationDaoJdbc extends AbstractResourceDaoJdbc<OrganizationAffiliation>
		implements OrganizationAffiliationDao
{
	public OrganizationAffiliationDaoJdbc(DataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, OrganizationAffiliation.class, "organization_affiliations",
				"organization_affiliation", "organization_affiliation_id", OrganizationAffiliationUserFilter::new,
				with(OrganizationAffiliationActive::new, OrganizationAffiliationIdentifier::new), with());
	}

	@Override
	protected OrganizationAffiliation copy(OrganizationAffiliation resource)
	{
		return resource.copy();
	}
}
