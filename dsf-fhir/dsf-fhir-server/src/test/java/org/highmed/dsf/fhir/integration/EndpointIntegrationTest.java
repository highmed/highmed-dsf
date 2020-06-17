package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.dao.EndpointDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;

public class EndpointIntegrationTest extends AbstractIntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointIntegrationTest.class);

	@Test
	public void testSearchAll() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Endpoint.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(2, searchBundle.getTotal());

		assertNotNull(searchBundle.getEntry());
		assertEquals(2, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Endpoint);
		assertNotNull(searchBundle.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, searchBundle.getEntry().get(0).getSearch().getMode());

		assertNotNull(searchBundle.getEntry().get(1));
		assertNotNull(searchBundle.getEntry().get(1).getResource());
		assertTrue(searchBundle.getEntry().get(1).getResource() instanceof Endpoint);
		assertNotNull(searchBundle.getEntry().get(1).getSearch());
		assertEquals(SearchEntryMode.MATCH, searchBundle.getEntry().get(1).getSearch().getMode());
	}

	@Test(expected = WebApplicationException.class)
	public void testSearchWithUnsupportedQueryParameterStrictHandling() throws Exception
	{
		try
		{
			getWebserviceClient().searchWithStrictHandling(Endpoint.class,
					Map.of("not-supported-parameter", Collections.singletonList("not-supported-parameter-value")));
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testSearchWithUnsupportedQueryParameterLenientHandling() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Endpoint.class,
				Map.of("not-supported-parameter", Collections.singletonList("not-supported-parameter-value")));

		assertNotNull(searchBundle.getEntry());
		assertEquals(3, searchBundle.getEntry().size());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Endpoint);
		assertNotNull(searchBundle.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, searchBundle.getEntry().get(0).getSearch().getMode());

		assertNotNull(searchBundle.getEntry().get(1));
		assertNotNull(searchBundle.getEntry().get(1).getResource());
		assertTrue(searchBundle.getEntry().get(1).getResource() instanceof Endpoint);
		assertNotNull(searchBundle.getEntry().get(1).getSearch());
		assertEquals(SearchEntryMode.MATCH, searchBundle.getEntry().get(1).getSearch().getMode());

		assertNotNull(searchBundle.getEntry().get(2));
		assertNotNull(searchBundle.getEntry().get(2).getResource());
		assertTrue(searchBundle.getEntry().get(2).getResource() instanceof OperationOutcome);
		assertNotNull(searchBundle.getEntry().get(2).getSearch());
		assertEquals(SearchEntryMode.OUTCOME, searchBundle.getEntry().get(2).getSearch().getMode());
	}

	@Test
	public void testSearchEndpointIncludeOrganization() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Endpoint.class,
				Map.of("_include", Collections.singletonList("Endpoint:organization")));
		assertNotNull(searchBundle);
		assertEquals(2, searchBundle.getTotal());
		assertEquals(4, searchBundle.getEntry().size());

		BundleEntryComponent eptEntry1 = searchBundle.getEntry().get(0);
		assertNotNull(eptEntry1);
		assertEquals(SearchEntryMode.MATCH, eptEntry1.getSearch().getMode());
		assertNotNull(eptEntry1.getResource());
		assertTrue(eptEntry1.getResource() instanceof Endpoint);

		BundleEntryComponent eptEntry2 = searchBundle.getEntry().get(1);
		assertNotNull(eptEntry2);
		assertEquals(SearchEntryMode.MATCH, eptEntry2.getSearch().getMode());
		assertNotNull(eptEntry2.getResource());
		assertTrue(eptEntry2.getResource() instanceof Endpoint);

		BundleEntryComponent orgEntry1 = searchBundle.getEntry().get(2);
		assertNotNull(orgEntry1);
		assertEquals(SearchEntryMode.INCLUDE, orgEntry1.getSearch().getMode());
		assertNotNull(orgEntry1.getResource());
		assertTrue(orgEntry1.getResource() instanceof Organization);

		BundleEntryComponent orgEntry2 = searchBundle.getEntry().get(3);
		assertNotNull(orgEntry2);
		assertEquals(SearchEntryMode.INCLUDE, orgEntry2.getSearch().getMode());
		assertNotNull(orgEntry2.getResource());
		assertTrue(orgEntry2.getResource() instanceof Organization);
	}

	@Test
	public void testSearchEndpointRevIncludeOrganization() throws Exception
	{
		Bundle searchBundle = getWebserviceClient().search(Endpoint.class,
				Map.of("_revinclude", Collections.singletonList("Organization:endpoint")));
		assertNotNull(searchBundle);
		assertEquals(2, searchBundle.getTotal());
		assertEquals(4, searchBundle.getEntry().size());

		BundleEntryComponent eptEntry1 = searchBundle.getEntry().get(0);
		assertNotNull(eptEntry1);
		assertEquals(SearchEntryMode.MATCH, eptEntry1.getSearch().getMode());
		assertNotNull(eptEntry1.getResource());
		assertTrue(eptEntry1.getResource() instanceof Endpoint);

		BundleEntryComponent eptEntry2 = searchBundle.getEntry().get(1);
		assertNotNull(eptEntry2);
		assertEquals(SearchEntryMode.MATCH, eptEntry2.getSearch().getMode());
		assertNotNull(eptEntry2.getResource());
		assertTrue(eptEntry2.getResource() instanceof Endpoint);

		BundleEntryComponent orgEntry1 = searchBundle.getEntry().get(2);
		assertNotNull(orgEntry1);
		assertEquals(SearchEntryMode.INCLUDE, orgEntry1.getSearch().getMode());
		assertNotNull(orgEntry1.getResource());
		assertTrue(orgEntry1.getResource() instanceof Organization);

		BundleEntryComponent orgEntry2 = searchBundle.getEntry().get(3);
		assertNotNull(orgEntry2);
		assertEquals(SearchEntryMode.INCLUDE, orgEntry2.getSearch().getMode());
		assertNotNull(orgEntry2.getResource());
		assertTrue(orgEntry2.getResource() instanceof Organization);
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
		endpoint.setStatus(EndpointStatus.ACTIVE);
		endpoint.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/endpoint-identifier")
				.setValue("foo-bar-baz.test.bla-bla.de");
		endpoint.getManagingOrganization().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier").setValue("bla-bla.de");
		endpoint.getConnectionType().setSystem("http://terminology.hl7.org/CodeSystem/endpoint-connection-type")
				.setCode("hl7-fhir-rest");
		endpoint.getPayloadTypeFirstRep().getCodingFirstRep().setSystem("http://hl7.org/fhir/resource-types")
				.setCode("Task");
		endpoint.addPayloadMimeTypeElement().setSystem("urn:ietf:bcp:13").setValue(Constants.CT_FHIR_XML_NEW);
		endpoint.addPayloadMimeTypeElement().setSystem("urn:ietf:bcp:13").setValue(Constants.CT_FHIR_JSON_NEW);
		endpoint.setAddress("https://foo-bar-baz.test.bla-bla.de/fhir");

		logger.debug("endpoint: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(endpoint));

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

		String orgTempId = "urn:uuid:" + UUID.randomUUID().toString();
		String endTempId = "urn:uuid:" + UUID.randomUUID().toString();

		Organization organization = new Organization();
		organization.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		organization.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/organization-identifier")
				.setValue("bla-bla.de");
		organization.addExtension("http://highmed.org/fhir/StructureDefinition/certificate-thumbprint", new StringType(
				"6b83a92506d67265697c74f50a9cac0ec7182adcc5302e5ed487ae1a782fe278f5ca79808c971e061fadded2c303a2223140ef3450d1d27717dd704a823f95e9"));
		organization.addEndpoint().setReference(endTempId);

		Endpoint endpoint = new Endpoint();
		endpoint.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/highmed-endpoint");
		endpoint.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		endpoint.addIdentifier().setSystem("http://highmed.org/fhir/NamingSystem/endpoint-identifier")
				.setValue("foo-bar-baz.test.bla-bla.de");
		endpoint.setStatus(EndpointStatus.ACTIVE);
		endpoint.getConnectionType().setSystem("http://terminology.hl7.org/CodeSystem/endpoint-connection-type")
				.setCode("hl7-fhir-rest");
		endpoint.getManagingOrganization().setReference(orgTempId);
		endpoint.getPayloadTypeFirstRep().getCodingFirstRep().setSystem("http://hl7.org/fhir/resource-types")
				.setCode("Task");
		endpoint.addPayloadMimeTypeElement().setSystem("urn:ietf:bcp:13").setValue(Constants.CT_FHIR_XML_NEW);
		endpoint.addPayloadMimeTypeElement().setSystem("urn:ietf:bcp:13").setValue(Constants.CT_FHIR_JSON_NEW);
		endpoint.setAddress("https://foo-bar-baz.test.bla-bla.de/fhir");

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(orgTempId).setResource(organization).getRequest().setMethod(HTTPVerb.POST)
				.setUrl("Organization");
		bundle.addEntry().setFullUrl(endTempId).setResource(endpoint).getRequest().setMethod(HTTPVerb.POST)
				.setUrl("Endpoint");
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl(
				"Endpoint?identifier=http://highmed.org/fhir/NamingSystem/endpoint-identifier|foo-bar-baz.test.bla-bla.de");

		logger.debug("bundle: {}", context.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle));
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
		assertTrue(readEdp.getManagingOrganization().hasReference());
	}
}
