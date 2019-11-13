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
		assertEquals(1, searchBundle.getTotal());
	}

	@Test
	public void testSearchOrganizationIncludeEndpoint() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Organization.class,
				Map.of("_include", Collections.singletonList("Organization:endpoint")));
		assertNotNull(searchBundle);
		assertEquals(1, searchBundle.getTotal());
		assertEquals(2, searchBundle.getEntry().size());

		BundleEntryComponent orgEntry = searchBundle.getEntry().get(0);
		assertNotNull(orgEntry);
		assertEquals(SearchEntryMode.MATCH, orgEntry.getSearch().getMode());
		assertNotNull(orgEntry.getResource());
		assertTrue(orgEntry.getResource() instanceof Organization);

		BundleEntryComponent eptEntry = searchBundle.getEntry().get(1);
		assertNotNull(eptEntry);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry.getSearch().getMode());
		assertNotNull(eptEntry.getResource());
		assertTrue(eptEntry.getResource() instanceof Endpoint);
	}
}
