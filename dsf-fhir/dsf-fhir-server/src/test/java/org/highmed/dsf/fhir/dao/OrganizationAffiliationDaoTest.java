package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.highmed.dsf.fhir.dao.jdbc.OrganizationAffiliationDaoJdbc;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

public class OrganizationAffiliationDaoTest
		extends AbstractResourceDaoTest<OrganizationAffiliation, OrganizationAffiliationDao>
{
	private static final String identifyierSystem = "http://foo.com/ids";
	private static final String identifierValue = "id";
	private static final boolean active = true;

	public OrganizationAffiliationDaoTest()
	{
		super(OrganizationAffiliation.class, OrganizationAffiliationDaoJdbc::new);
	}

	@Override
	protected OrganizationAffiliation createResource()
	{
		OrganizationAffiliation organizationAffiliation = new OrganizationAffiliation();
		organizationAffiliation.addIdentifier().setSystem(identifyierSystem).setValue(identifierValue);
		return organizationAffiliation;
	}

	@Override
	protected void checkCreated(OrganizationAffiliation resource)
	{
		assertTrue(resource.hasIdentifier());
		assertEquals(identifyierSystem, resource.getIdentifierFirstRep().getSystem());
		assertEquals(identifierValue, resource.getIdentifierFirstRep().getValue());
	}

	@Override
	protected OrganizationAffiliation updateResource(OrganizationAffiliation resource)
	{
		resource.setActive(active);
		return resource;
	}

	@Override
	protected void checkUpdates(OrganizationAffiliation resource)
	{
		assertEquals(active, resource.getActive());
	}
}
