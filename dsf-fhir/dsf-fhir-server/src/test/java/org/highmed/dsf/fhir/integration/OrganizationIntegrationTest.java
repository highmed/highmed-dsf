package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;

public class OrganizationIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testSearchAll() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Organization.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(2, searchBundle.getTotal());
		assertTrue(searchBundle.getEntryFirstRep().getResource() instanceof Organization);
	}

	@Test
	public void testSearchOrganizationIncludeEndpoint() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Organization.class,
				Map.of("_include", Collections.singletonList("Organization:endpoint")));
		assertNotNull(searchBundle);
		assertEquals(2, searchBundle.getTotal());
		assertEquals(4, searchBundle.getEntry().size());

		BundleEntryComponent orgEntry1 = searchBundle.getEntry().get(0);
		assertNotNull(orgEntry1);
		assertEquals(SearchEntryMode.MATCH, orgEntry1.getSearch().getMode());
		assertNotNull(orgEntry1.getResource());
		assertTrue(orgEntry1.getResource() instanceof Organization);

		BundleEntryComponent orgEntry2 = searchBundle.getEntry().get(1);
		assertNotNull(orgEntry2);
		assertEquals(SearchEntryMode.MATCH, orgEntry2.getSearch().getMode());
		assertNotNull(orgEntry2.getResource());
		assertTrue(orgEntry2.getResource() instanceof Organization);

		BundleEntryComponent eptEntry1 = searchBundle.getEntry().get(2);
		assertNotNull(eptEntry1);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry1.getSearch().getMode());
		assertNotNull(eptEntry1.getResource());
		assertTrue(eptEntry1.getResource() instanceof Endpoint);
		
		BundleEntryComponent eptEntry2 = searchBundle.getEntry().get(3);
		assertNotNull(eptEntry2);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry2.getSearch().getMode());
		assertNotNull(eptEntry2.getResource());
		assertTrue(eptEntry2.getResource() instanceof Endpoint);
	}

	@Test
	public void testSearchOrganizationRevIncludeEndpoint() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Organization.class,
				Map.of("_revinclude", Collections.singletonList("Endpoint:organization")));
		assertNotNull(searchBundle);
		assertEquals(2, searchBundle.getTotal());
		assertEquals(4, searchBundle.getEntry().size());

		BundleEntryComponent orgEntry1 = searchBundle.getEntry().get(0);
		assertNotNull(orgEntry1);
		assertEquals(SearchEntryMode.MATCH, orgEntry1.getSearch().getMode());
		assertNotNull(orgEntry1.getResource());
		assertTrue(orgEntry1.getResource() instanceof Organization);

		BundleEntryComponent orgEntry2 = searchBundle.getEntry().get(1);
		assertNotNull(orgEntry2);
		assertEquals(SearchEntryMode.MATCH, orgEntry2.getSearch().getMode());
		assertNotNull(orgEntry2.getResource());
		assertTrue(orgEntry2.getResource() instanceof Organization);

		BundleEntryComponent eptEntry1 = searchBundle.getEntry().get(2);
		assertNotNull(eptEntry1);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry1.getSearch().getMode());
		assertNotNull(eptEntry1.getResource());
		assertTrue(eptEntry1.getResource() instanceof Endpoint);
		
		BundleEntryComponent eptEntry2 = searchBundle.getEntry().get(3);
		assertNotNull(eptEntry2);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry2.getSearch().getMode());
		assertNotNull(eptEntry2.getResource());
		assertTrue(eptEntry2.getResource() instanceof Endpoint);
	}
}
