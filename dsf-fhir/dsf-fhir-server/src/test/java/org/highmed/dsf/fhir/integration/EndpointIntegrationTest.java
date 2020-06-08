package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.highmed.dsf.fhir.dao.EndpointDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class EndpointIntegrationTest extends AbstractIntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointIntegrationTest.class);

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

	@Test
	public void testCreateReadWithLogicalReference() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Organization organization = new Organization();
		organization.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		organization.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("bla-bla.de");
		organization.addExtension("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint", new StringType(
				"6b83a92506d67265697c74f50a9cac0ec7182adcc5302e5ed487ae1a782fe278f5ca79808c971e061fadded2c303a2223140ef3450d1d27717dd704a823f95e9"));

		Organization createdOrg = getWebserviceClient().create(organization);
		logger.debug("Organization: {}",
				context.newXmlParser().setPrettyPrint(true).encodeResourceToString(createdOrg));

		Endpoint endpoint = new Endpoint();
		endpoint.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		endpoint.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/endpoint-identifier")
				.setValue("foo-bar-baz.test.bla-bla.de");
		endpoint.setAddress("https://foo-bar-baz.test.bla-bla.de/fhir");
		endpoint.getManagingOrganization().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("bla-bla.de");

		Endpoint createdEdp = getWebserviceClient().create(endpoint);
		assertNotNull(createdEdp);
		logger.debug("created: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(createdEdp));
		assertTrue(createdEdp.hasIdElement());
		assertTrue(createdEdp.getIdElement().hasIdPart());
		assertTrue(createdEdp.hasManagingOrganization());
		assertTrue(createdEdp.getManagingOrganization().hasIdentifier());
		assertFalse(createdEdp.getManagingOrganization().hasReference());

		EndpointDao dao = getSpringWebApplicationContext().getBean(EndpointDao.class);
		Optional<Endpoint> daoEdp = dao.read(UUID.fromString(createdEdp.getIdElement().getIdPart()));
		assertTrue(daoEdp.isPresent());
		logger.debug("db: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(daoEdp.get()));
		assertTrue(daoEdp.get().hasIdElement());
		assertTrue(daoEdp.get().getIdElement().hasIdPart());
		assertTrue(daoEdp.get().hasManagingOrganization());
		assertTrue(daoEdp.get().getManagingOrganization().hasIdentifier());
		assertTrue(daoEdp.get().getManagingOrganization().hasReference());

		Endpoint readEdp = getWebserviceClient().read(Endpoint.class, createdEdp.getIdElement().getIdPart());
		assertNotNull(readEdp);
		logger.debug("read: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(readEdp));
		assertTrue(readEdp.hasIdElement());
		assertTrue(readEdp.getIdElement().hasIdPart());
		assertTrue(readEdp.hasManagingOrganization());
		assertTrue(readEdp.getManagingOrganization().hasIdentifier());
		assertFalse(readEdp.getManagingOrganization().hasReference());

		Endpoint vReadEdp = getWebserviceClient().read(Endpoint.class, createdEdp.getIdElement().getIdPart(),
				createdEdp.getIdElement().getVersionIdPart());
		assertNotNull(vReadEdp);
		logger.debug("vread: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(vReadEdp));
		assertTrue(vReadEdp.hasIdElement());
		assertTrue(vReadEdp.getIdElement().hasIdPart());
		assertTrue(vReadEdp.hasManagingOrganization());
		assertTrue(vReadEdp.getManagingOrganization().hasIdentifier());
		assertFalse(vReadEdp.getManagingOrganization().hasReference());
	}

	@Test
	public void testCreateReadWithLogicalReferenceViaBundle() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Organization organization = new Organization();
		organization.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		organization.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("bla-bla.de");
		organization.addExtension("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint", new StringType(
				"6b83a92506d67265697c74f50a9cac0ec7182adcc5302e5ed487ae1a782fe278f5ca79808c971e061fadded2c303a2223140ef3450d1d27717dd704a823f95e9"));

		Endpoint endpoint = new Endpoint();
		endpoint.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		endpoint.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/endpoint-identifier")
				.setValue("foo-bar-baz.test.bla-bla.de");
		endpoint.setAddress("https://foo-bar-baz.test.bla-bla.de/fhir");
		endpoint.getManagingOrganization().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("bla-bla.de");

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(organization).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Organization");
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(endpoint).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Endpoint");
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl(
				"Endpoint?identifier=http://highmed.org/fhir/NamingSystem/endpoint-identifier|foo-bar-baz.test.bla-bla.de");

		Bundle postBundle = getWebserviceClient().postBundle(bundle);
		logger.debug("post: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(postBundle));

		assertTrue(postBundle.hasEntry());
		assertEquals(3, postBundle.getEntry().size());
		assertTrue(postBundle.getEntry().get(2).hasResource());
		assertTrue(postBundle.getEntry().get(2).getResource() instanceof Endpoint);

		Endpoint readEdp = (Endpoint) postBundle.getEntry().get(2).getResource();
		assertNotNull(readEdp);
		assertTrue(readEdp.hasIdElement());
		assertTrue(readEdp.getIdElement().hasIdPart());
		assertTrue(readEdp.hasManagingOrganization());
		assertTrue(readEdp.getManagingOrganization().hasIdentifier());
		assertFalse(readEdp.getManagingOrganization().hasReference());
	}
}
