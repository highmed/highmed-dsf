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
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class EndpointIntegrationTest extends AbstractIntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointIntegrationTest.class);

	private Endpoint createEndpoint()
	{
		Endpoint endpoint = new Endpoint();
		endpoint.addIdentifier().setSystem("http://highmed.org/sid/endpoint-identifier")
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
		return endpoint;
	}

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
		Endpoint endpoint = createEndpoint();
		getReadAccessHelper().addLocal(endpoint);

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
		getReadAccessHelper().addAll(organization);
		organization.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("bla-bla.de");
		organization.addExtension("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint",
				new StringType(
						"6b83a92506d67265697c74f50a9cac0ec7182adcc5302e5ed487ae1a782fe278f5ca79808c971e061fadded2c303a2223140ef3450d1d27717dd704a823f95e9"));

		Organization createdOrg = getWebserviceClient().create(organization);
		logger.debug("Organization: {}",
				context.newXmlParser().setPrettyPrint(true).encodeResourceToString(createdOrg));

		Endpoint endpoint = createEndpoint();
		getReadAccessHelper().addAll(endpoint);
		endpoint.getManagingOrganization().setType("Organization").getIdentifier()
				.setSystem("http://highmed.org/sid/organization-identifier").setValue("bla-bla.de");

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
		getReadAccessHelper().addAll(organization);
		organization.addIdentifier().setSystem("http://highmed.org/sid/organization-identifier").setValue("bla-bla.de");
		organization.addExtension("http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint",
				new StringType(
						"6b83a92506d67265697c74f50a9cac0ec7182adcc5302e5ed487ae1a782fe278f5ca79808c971e061fadded2c303a2223140ef3450d1d27717dd704a823f95e9"));
		organization.addEndpoint().setReference(endTempId);

		Endpoint endpoint = createEndpoint();
		getReadAccessHelper().addAll(endpoint);
		endpoint.getMeta().addProfile("http://highmed.org/fhir/StructureDefinition/endpoint");
		endpoint.getManagingOrganization().setReference(orgTempId);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(orgTempId).setResource(organization).getRequest().setMethod(HTTPVerb.POST)
				.setUrl("Organization");
		bundle.addEntry().setFullUrl(endTempId).setResource(endpoint).getRequest().setMethod(HTTPVerb.POST)
				.setUrl("Endpoint");
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET)
				.setUrl("Endpoint?identifier=http://highmed.org/sid/endpoint-identifier|foo-bar-baz.test.bla-bla.de");

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

	@Test
	public void testCreateWithRelativeLiteralReferenceNotExisting() throws Exception
	{
		Endpoint endpoint = createEndpoint();
		getReadAccessHelper().addLocal(endpoint);
		endpoint.getManagingOrganization().setReference("Organization/" + UUID.randomUUID());

		expectForbidden(() -> getWebserviceClient().create(endpoint));
	}

	@Test
	public void testCreateViaBundleWithRelativeLiteralReferenceNotExisting() throws Exception
	{
		Endpoint endpoint = createEndpoint();
		getReadAccessHelper().addLocal(endpoint);
		endpoint.getManagingOrganization().setReference("Organization/" + UUID.randomUUID());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(endpoint).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Endpoint");

		expectForbidden(() -> getWebserviceClient().postBundle(bundle));
	}

	@Test
	public void testUpdateWithRelativeLiteralReferenceNotExisting() throws Exception
	{
		OrganizationDao organizationDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		EndpointDao endpointDao = getSpringWebApplicationContext().getBean(EndpointDao.class);

		SearchQuery<Organization> query = organizationDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("identifier",
						Collections.singletonList("http://highmed.org/sid/organization-identifier|Test_Organization")));
		PartialResult<Organization> organizationResult = organizationDao.search(query);
		assertNotNull(organizationResult);
		assertEquals(1, organizationResult.getTotal());
		assertNotNull(organizationResult.getPartialResult());
		assertEquals(1, organizationResult.getPartialResult().size());
		assertNotNull(organizationResult.getPartialResult().get(0));

		IdType organizationId = organizationResult.getPartialResult().get(0).getIdElement().toVersionless();

		Endpoint endpoint = createEndpoint();
		getReadAccessHelper().addLocal(endpoint);
		endpoint.getManagingOrganization().setReference(organizationId.getValue());

		Endpoint createdEndpoint = endpointDao.create(endpoint);
		createdEndpoint.getManagingOrganization().setReference("Organization/" + UUID.randomUUID());

		expectForbidden(() -> getWebserviceClient().update(createdEndpoint));
	}

	@Test
	public void testUpdateViaBundleWithRelativeLiteralReferenceNotExisting() throws Exception
	{
		OrganizationDao organizationDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		EndpointDao endpointDao = getSpringWebApplicationContext().getBean(EndpointDao.class);

		SearchQuery<Organization> query = organizationDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("identifier",
						Collections.singletonList("http://highmed.org/sid/organization-identifier|Test_Organization")));
		PartialResult<Organization> organizationResult = organizationDao.search(query);
		assertNotNull(organizationResult);
		assertEquals(1, organizationResult.getTotal());
		assertNotNull(organizationResult.getPartialResult());
		assertEquals(1, organizationResult.getPartialResult().size());
		assertNotNull(organizationResult.getPartialResult().get(0));

		IdType organizationId = organizationResult.getPartialResult().get(0).getIdElement().toVersionless();

		Endpoint endpoint = createEndpoint();
		getReadAccessHelper().addLocal(endpoint);
		endpoint.getManagingOrganization().setReference(organizationId.getValue());

		Endpoint createdEndpoint = endpointDao.create(endpoint);
		createdEndpoint.getManagingOrganization().setReference("Organization/" + UUID.randomUUID());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry()
				.setFullUrl(createdEndpoint.getIdElement()
						.withServerBase(getWebserviceClient().getBaseUrl(), "Endpoint").toVersionless().getValue())
				.setResource(createdEndpoint).getRequest().setMethod(HTTPVerb.PUT)
				.setUrl("Endpoint/" + createdEndpoint.getIdElement().getIdPart());

		expectForbidden(() -> getWebserviceClient().postBundle(bundle));
	}
}
