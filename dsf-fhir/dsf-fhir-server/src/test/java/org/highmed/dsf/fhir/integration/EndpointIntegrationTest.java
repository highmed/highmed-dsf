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
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.Organization;
import org.junit.Test;

public class EndpointIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testSearchAll() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Endpoint.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(1, searchBundle.getTotal());
		assertTrue(searchBundle.getEntryFirstRep().getResource() instanceof Endpoint);
	}

	@Test
	public void testSearchEndpointIncludeOrganization() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Endpoint.class,
				Map.of("_include", Collections.singletonList("Endpoint:organization")));
		assertNotNull(searchBundle);
		assertEquals(1, searchBundle.getTotal());
		assertEquals(2, searchBundle.getEntry().size());

		BundleEntryComponent eptEntry = searchBundle.getEntry().get(0);
		assertNotNull(eptEntry);
		assertEquals(SearchEntryMode.MATCH, eptEntry.getSearch().getMode());
		assertNotNull(eptEntry.getResource());
		assertTrue(eptEntry.getResource() instanceof Endpoint);

		BundleEntryComponent orgEntry = searchBundle.getEntry().get(1);
		assertNotNull(orgEntry);
		assertEquals(SearchEntryMode.INCLUDE, orgEntry.getSearch().getMode());
		assertNotNull(orgEntry.getResource());
		assertTrue(orgEntry.getResource() instanceof Organization);
	}

	@Test
	public void testSearchEndpointRevIncludeOrganization() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Endpoint.class,
				Map.of("_revinclude", Collections.singletonList("Organization:endpoint")));
		assertNotNull(searchBundle);
		assertEquals(1, searchBundle.getTotal());
		assertEquals(2, searchBundle.getEntry().size());

		BundleEntryComponent orgEntry = searchBundle.getEntry().get(0);
		assertNotNull(orgEntry);
		assertEquals(SearchEntryMode.MATCH, orgEntry.getSearch().getMode());
		assertNotNull(orgEntry.getResource());
		assertTrue(orgEntry.getResource() instanceof Endpoint);

		BundleEntryComponent eptEntry = searchBundle.getEntry().get(1);
		assertNotNull(eptEntry);
		assertEquals(SearchEntryMode.INCLUDE, eptEntry.getSearch().getMode());
		assertNotNull(eptEntry.getResource());
		assertTrue(eptEntry.getResource() instanceof Organization);
	}

	@Test
	public void testCreateValidByLocalUser() throws Exception
	{
		Endpoint endpoint = new Endpoint();
		endpoint.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		endpoint.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/endpoint-identifier")
				.setValue("foo-bar-baz.test.bla-bla.de");
		endpoint.setStatus(EndpointStatus.ACTIVE);
		endpoint.getConnectionType().setSystem("http://terminology.hl7.org/CodeSystem/endpoint-connection-type")
				.setCode("hl7-fhir-rest");
		endpoint.setName("Test TTP");
		endpoint.getPayloadTypeFirstRep().getCodingFirstRep().setSystem("http://hl7.org/fhir/resource-types")
				.setCode("Task");
		endpoint.addPayloadMimeType("application/fhir+json");
		endpoint.addPayloadMimeType("application/fhir+xml");
		endpoint.setAddress("https://foo-bar-baz.test.bla-bla.de/fhir");

		Endpoint created = getWebserviceClient().create(endpoint);
		assertNotNull(created);
		assertNotNull(created.getIdElement().getIdPart());
		assertNotNull(created.getIdElement().getVersionIdPart());
	}
}
