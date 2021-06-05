package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;

public class OrganizationIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testSearchAll() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Organization.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(3, searchBundle.getTotal());
		assertTrue(searchBundle.getEntryFirstRep().getResource() instanceof Organization);
	}

	@Test
	public void testSearchOrganizationIncludeEndpoint() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Organization.class,
				Map.of("_include", Collections.singletonList("Organization:endpoint")));
		assertNotNull(searchBundle);
		assertEquals(3, searchBundle.getTotal());
		assertEquals(5, searchBundle.getEntry().size());

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

		BundleEntryComponent orgEntry3 = searchBundle.getEntry().get(2);
		assertNotNull(orgEntry3);
		assertEquals(SearchEntryMode.MATCH, orgEntry3.getSearch().getMode());
		assertNotNull(orgEntry3.getResource());
		assertTrue(orgEntry3.getResource() instanceof Organization);

		BundleEntryComponent eptEntry1 = searchBundle.getEntry().get(3);
		assertNotNull(eptEntry1);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry1.getSearch().getMode());
		assertNotNull(eptEntry1.getResource());
		assertTrue(eptEntry1.getResource() instanceof Endpoint);

		BundleEntryComponent eptEntry2 = searchBundle.getEntry().get(4);
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
		assertEquals(3, searchBundle.getTotal());
		assertEquals(5, searchBundle.getEntry().size());

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

		BundleEntryComponent orgEntry3 = searchBundle.getEntry().get(2);
		assertNotNull(orgEntry3);
		assertEquals(SearchEntryMode.MATCH, orgEntry3.getSearch().getMode());
		assertNotNull(orgEntry3.getResource());
		assertTrue(orgEntry3.getResource() instanceof Organization);

		BundleEntryComponent eptEntry1 = searchBundle.getEntry().get(3);
		assertNotNull(eptEntry1);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry1.getSearch().getMode());
		assertNotNull(eptEntry1.getResource());
		assertTrue(eptEntry1.getResource() instanceof Endpoint);

		BundleEntryComponent eptEntry2 = searchBundle.getEntry().get(4);
		assertNotNull(eptEntry2);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry2.getSearch().getMode());
		assertNotNull(eptEntry2.getResource());
		assertTrue(eptEntry2.getResource() instanceof Endpoint);
	}

	@Test
	public void testUpdateOrganizationWithNewThumbprint() throws Exception
	{
		Bundle bundle = getWebserviceClient().search(Organization.class, Map.of("identifier", Collections
				.singletonList("http://highmed.org/sid/organization-identifier|External_Test_Organization")));
		assertNotNull(bundle);
		assertEquals(1, bundle.getTotal());
		assertNotNull(bundle.getEntry());
		assertEquals(1, bundle.getEntry().size());
		assertNotNull(bundle.getEntry().get(0));
		assertNotNull(bundle.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, bundle.getEntry().get(0).getSearch().getMode());
		assertNotNull(bundle.getEntry().get(0).getResource());
		assertTrue(bundle.getEntry().get(0).getResource() instanceof Organization);

		Organization org = (Organization) bundle.getEntryFirstRep().getResource();
		List<Extension> thumbprints = org
				.getExtensionsByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		assertNotNull(thumbprints);
		assertEquals(1, thumbprints.size());

		thumbprints.get(0).setValue(new StringType(
				"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
		getWebserviceClient().update(org);
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateOrganizationWithExistingThumbprint() throws Exception
	{
		Bundle bundle1 = getWebserviceClient().search(Organization.class, Map.of("identifier",
				Collections.singletonList("http://highmed.org/sid/organization-identifier|Test_Organization")));
		assertNotNull(bundle1);
		assertEquals(1, bundle1.getTotal());
		assertNotNull(bundle1.getEntry());
		assertEquals(1, bundle1.getEntry().size());
		assertNotNull(bundle1.getEntry().get(0));
		assertNotNull(bundle1.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, bundle1.getEntry().get(0).getSearch().getMode());
		assertNotNull(bundle1.getEntry().get(0).getResource());
		assertTrue(bundle1.getEntry().get(0).getResource() instanceof Organization);

		Organization org1 = (Organization) bundle1.getEntryFirstRep().getResource();
		List<Extension> thumbprints1 = org1
				.getExtensionsByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		assertNotNull(thumbprints1);
		assertEquals(1, thumbprints1.size());

		String existingThumbprint = ((StringType) thumbprints1.get(0).getValue()).getValue();

		Bundle bundle2 = getWebserviceClient().search(Organization.class, Map.of("identifier", Collections
				.singletonList("http://highmed.org/sid/organization-identifier|External_Test_Organization")));
		assertNotNull(bundle2);
		assertEquals(1, bundle2.getTotal());
		assertNotNull(bundle2.getEntry());
		assertEquals(1, bundle2.getEntry().size());
		assertNotNull(bundle2.getEntry().get(0));
		assertNotNull(bundle2.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, bundle2.getEntry().get(0).getSearch().getMode());
		assertNotNull(bundle2.getEntry().get(0).getResource());
		assertTrue(bundle2.getEntry().get(0).getResource() instanceof Organization);

		Organization org2 = (Organization) bundle2.getEntryFirstRep().getResource();
		List<Extension> thumbprints2 = org2
				.getExtensionsByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		assertNotNull(thumbprints2);
		assertEquals(1, thumbprints2.size());

		thumbprints2.get(0).setValue(new StringType(existingThumbprint));

		try
		{
			getWebserviceClient().update(org2);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateOrganizationWithExistingIdentifier() throws Exception
	{
		Bundle bundle = getWebserviceClient().search(Organization.class, Map.of("identifier", Collections
				.singletonList("http://highmed.org/sid/organization-identifier|External_Test_Organization")));
		assertNotNull(bundle);
		assertEquals(1, bundle.getTotal());
		assertNotNull(bundle.getEntry());
		assertEquals(1, bundle.getEntry().size());
		assertNotNull(bundle.getEntry().get(0));
		assertNotNull(bundle.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, bundle.getEntry().get(0).getSearch().getMode());
		assertNotNull(bundle.getEntry().get(0).getResource());
		assertTrue(bundle.getEntry().get(0).getResource() instanceof Organization);

		Organization org = (Organization) bundle.getEntryFirstRep().getResource();
		org.getIdentifierFirstRep().setValue("Test_Organization");

		try
		{
			getWebserviceClient().update(org);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testUpdateOrganizationAddNewThumbprint() throws Exception
	{
		Bundle bundle = getWebserviceClient().search(Organization.class, Map.of("identifier", Collections
				.singletonList("http://highmed.org/sid/organization-identifier|External_Test_Organization")));
		assertNotNull(bundle);
		assertEquals(1, bundle.getTotal());
		assertNotNull(bundle.getEntry());
		assertEquals(1, bundle.getEntry().size());
		assertNotNull(bundle.getEntry().get(0));
		assertNotNull(bundle.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, bundle.getEntry().get(0).getSearch().getMode());
		assertNotNull(bundle.getEntry().get(0).getResource());
		assertTrue(bundle.getEntry().get(0).getResource() instanceof Organization);

		Organization org = (Organization) bundle.getEntryFirstRep().getResource();
		List<Extension> thumbprints = org
				.getExtensionsByUrl("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint");
		assertNotNull(thumbprints);
		assertEquals(1, thumbprints.size());

		Extension oldThumbprint = thumbprints.get(0);
		Extension newThumbprint = new Extension(
				"http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint", new StringType(
						"00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));

		org.setExtension(List.of(newThumbprint, oldThumbprint));
		getWebserviceClient().update(org);

		// test if authentication still works
		getExternalWebserviceClient().search(Organization.class, Collections.emptyMap());
	}

	@Test
	public void testSearchWithUnsupportedRevIncludeParameter() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Organization.class,
				Map.of("_revinclude", Collections.singletonList("Endpoint:foo")));
		assertNotNull(searchBundle);
		assertEquals(3, searchBundle.getTotal());
		assertEquals(4, searchBundle.getEntry().size());

		BundleEntryComponent outcomeEntry = searchBundle.getEntry().get(3);
		assertNotNull(outcomeEntry);
		assertEquals(SearchEntryMode.OUTCOME, outcomeEntry.getSearch().getMode());
		assertNotNull(outcomeEntry.getResource());
		assertTrue(outcomeEntry.getResource() instanceof OperationOutcome);
	}

	@Test(expected = WebApplicationException.class)
	public void testStrictSearchWithUnsupportedRevIncludeParameter() throws Exception
	{
		try
		{
			getWebserviceClient().searchWithStrictHandling(Organization.class,
					Map.of("_revinclude", Collections.singletonList("Endpoint:foo")));
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}
}
