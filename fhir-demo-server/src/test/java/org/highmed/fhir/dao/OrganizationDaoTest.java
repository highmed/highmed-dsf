package org.highmed.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.context.FhirContext;

public class OrganizationDaoTest extends AbstractDomainResourceDaoTest<Organization, OrganizationDao>
{
	private static final String name = "Demo Organization";
	private static final boolean active = true;

	public OrganizationDaoTest()
	{
		super(Organization.class);
	}

	@Override
	protected OrganizationDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new OrganizationDao(dataSource, fhirContext);
	}

	@Override
	protected Organization createResource()
	{
		Organization organization = new Organization();
		organization.setName(name);
		return organization;
	}

	@Override
	protected void checkCreated(Organization resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Organization updateResource(Organization resource)
	{
		resource.setActive(active);
		return resource;
	}

	@Override
	protected void checkUpdates(Organization resource)
	{
		assertEquals(active, resource.getActive());
	}
}
