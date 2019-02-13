package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationDao extends AbstractDomainResourceDao<Organization>
{
	public OrganizationDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Organization.class, "organizations", "organization", "organization_id");
	}

	@Override
	protected Organization copy(Organization resource)
	{
		return resource.copy();
	}

	public PartialResult<Organization> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
