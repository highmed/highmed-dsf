package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.search.PartialResult;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.Test;

public class BinaryIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testReadAllowedLocalUser() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		getWebserviceClient().read(Binary.class, created.getIdElement().getIdPart());
	}

	@Test
	public void testReadNotAllowedRemoteUser() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		expectForbidden(() -> getExternalWebserviceClient().read(Binary.class, created.getIdElement().getIdPart()));
	}

	@Test
	public void testReadAllowedLocalUserViaStream() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		try (InputStream in = getWebserviceClient().readBinary(created.getIdElement().getIdPart(),
				MediaType.TEXT_PLAIN_TYPE))
		{
			assertTrue(Arrays.equals(data, in.readAllBytes()));
		}
	}

	@Test
	public void testReadAllowedLocalUserViaStreamWithVersion() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		try (InputStream in = getWebserviceClient().readBinary(created.getIdElement().getIdPart(),
				created.getIdElement().getVersionIdPart(), MediaType.TEXT_PLAIN_TYPE))
		{
			assertTrue(Arrays.equals(data, in.readAllBytes()));
		}
	}

	@Test
	public void testReadAllowedLocalUserViaStreamAcceptWildcard() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		try (InputStream in = getWebserviceClient().readBinary(created.getIdElement().getIdPart(),
				MediaType.WILDCARD_TYPE))
		{
			assertTrue(Arrays.equals(data, in.readAllBytes()));
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testReadAllowedLocalUserViaStreamMediaTypeNotSupported() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		try
		{
			try (InputStream in = getWebserviceClient().readBinary(created.getIdElement().getIdPart(),
					MediaType.APPLICATION_XML_TYPE))
			{
				assertTrue(Arrays.equals(data, in.readAllBytes()));
			}
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testReadAllowedExternalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("External Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);
		getReadAccessHelper().addOrganization(binary, org);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		getExternalWebserviceClient().read(Binary.class, created.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedExternalUserViaSecurityContext() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("External Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);
		getReadAccessHelper().addOrganization(rs, org);

		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy createdRs = researchStudyDao.create(rs);
		assertNotNull(createdRs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		getExternalWebserviceClient().read(Binary.class, created.getIdElement().getIdPart());
	}

	@Test
	public void testReadNotAllowedExternalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);
		getReadAccessHelper().addOrganization(binary, org);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		expectForbidden(() -> getExternalWebserviceClient().read(Binary.class, created.getIdElement().getIdPart()));
	}

	@Test
	public void testReadAllowedLocalUserViaTransactionBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertNotNull(responseBundle.getEntry().get(0).getResource());
		assertTrue(responseBundle.getEntry().get(0).getResource() instanceof Binary);
		assertEquals(created.getIdElement().getIdPart(),
				responseBundle.getEntry().get(0).getResource().getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedExternalUserViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("External Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);
		getReadAccessHelper().addOrganization(binary, org);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertNotNull(responseBundle.getEntry().get(0).getResource());
		assertTrue(responseBundle.getEntry().get(0).getResource() instanceof Binary);
		assertEquals(created.getIdElement().getIdPart(),
				responseBundle.getEntry().get(0).getResource().getIdElement().getIdPart());
	}

	@Test
	public void testReadNotAllowedExternalUserViaTransactionBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl("Binary/" + created.getIdElement().getIdPart());

		expectForbidden(() -> getExternalWebserviceClient().postBundle(bundle));
	}

	@Test
	public void testReadAllowedLocalUserViaBatchBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertNotNull(responseBundle.getEntry().get(0).getResource());
		assertTrue(responseBundle.getEntry().get(0).getResource() instanceof Binary);
		assertEquals(created.getIdElement().getIdPart(),
				responseBundle.getEntry().get(0).getResource().getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedExternalUserViaBatchBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("External Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);
		getReadAccessHelper().addOrganization(binary, org);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertNotNull(responseBundle.getEntry().get(0).getResource());
		assertTrue(responseBundle.getEntry().get(0).getResource() instanceof Binary);
		assertEquals(created.getIdElement().getIdPart(),
				responseBundle.getEntry().get(0).getResource().getIdElement().getIdPart());
	}

	@Test
	public void testReadNotAllowedExternalUserViaBatchBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("403 Forbidden", responseBundle.getEntry().get(0).getResponse().getStatus());
	}

	@Test
	public void testHeadAllowedLocalUserViaTransactionBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.HEAD).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertFalse(responseBundle.getEntry().get(0).hasResource());
	}

	@Test
	public void testHeadAllowedExternalUserViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("External Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);
		getReadAccessHelper().addOrganization(binary, org);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.HEAD).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertFalse(responseBundle.getEntry().get(0).hasResource());
	}

	@Test
	public void testHeadNotAllowedExternalUserViaTransactionBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.HEAD).setUrl("Binary/" + created.getIdElement().getIdPart());

		expectForbidden(() -> getExternalWebserviceClient().postBundle(bundle));
	}

	@Test
	public void testHeadAllowedLocalUserViaBatchBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.HEAD).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertFalse(responseBundle.getEntry().get(0).hasResource());
	}

	@Test
	public void testHeadAllowedExternalUserViaBatchBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("External Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getTotal());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);
		getReadAccessHelper().addOrganization(binary, org);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.HEAD).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));
		assertFalse(responseBundle.getEntry().get(0).hasResource());
	}

	@Test
	public void testHeadNotAllowedExternalUserViaBatchBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.HEAD).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("403 Forbidden", responseBundle.getEntry().get(0).getResponse().getStatus());
	}

	@Test
	public void testCreateNotAllowedNoReadAccessTagOrSecurityContext() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);

		expectForbidden(() -> getWebserviceClient().create(binary));
	}

	@Test
	public void testCreateAllowedReadAccessTag() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		Binary created = getWebserviceClient().create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data, created.getData()));
	}

	@Test
	public void testCreateAllowedSecurityContext() throws Exception
	{
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		Binary created = getWebserviceClient().create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data, created.getData()));
	}

	@Test
	public void testCreateReturnMinimal() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		IdType created = getWebserviceClient().withMinimalReturn().create(binary);

		assertNotNull(created);
		assertNotNull(created.getBaseUrl());
		assertNotNull(created.getResourceType());
		assertNotNull(created.getIdPart());
		assertNotNull(created.getVersionIdPart());

		assertEquals(BASE_URL, created.getBaseUrl());
		assertEquals("Binary", created.getResourceType());
		assertEquals("1", created.getVersionIdPart());
	}

	@Test
	public void testCreateReturnOperationOutcome() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		OperationOutcome created = getWebserviceClient().withOperationOutcomeReturn().create(binary);

		assertNotNull(created);
	}

	@Test
	public void testCreateViaInputStream() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary created = getWebserviceClient().createBinary(new ByteArrayInputStream(data),
				MediaType.valueOf(contentType), createdRs.getIdElement().toVersionless().toString());

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());
	}

	@Test
	public void testCreateViaInputStreamReturnMinimal() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		IdType created = getWebserviceClient().withMinimalReturn().createBinary(new ByteArrayInputStream(data),
				MediaType.valueOf(contentType), createdRs.getIdElement().toVersionless().toString());

		assertNotNull(created);
		assertNotNull(created.getBaseUrl());
		assertNotNull(created.getResourceType());
		assertNotNull(created.getIdPart());
		assertNotNull(created.getVersionIdPart());

		assertEquals(BASE_URL, created.getBaseUrl());
		assertEquals("Binary", created.getResourceType());
		assertEquals("1", created.getVersionIdPart());
	}

	@Test
	public void testCreateViaInputStreamReturnOperationOutcome() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		OperationOutcome created = getWebserviceClient().withOperationOutcomeReturn().createBinary(
				new ByteArrayInputStream(data), MediaType.valueOf(contentType),
				createdRs.getIdElement().toVersionless().toString());

		assertNotNull(created);
	}

	@Test
	public void testCreateNotAllowedExternalUser() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		expectForbidden(() -> getExternalWebserviceClient().create(binary));
	}

	@Test
	public void testCreateSecurityContextOrgNotExisting() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference("Group/" + UUID.randomUUID().toString()));

		expectForbidden(() -> getWebserviceClient().create(binary));
	}

	@Test
	public void testCreateSecurityContextLogicalReference() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		rs.addIdentifier().setSystem("http://highmed.org/sid/research-study-identifier")
				.setValue(UUID.randomUUID().toString());
		getReadAccessHelper().addLocal(rs);

		researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		Reference securityContext = new Reference();
		securityContext.setType("ResearchStudy").getIdentifier()
				.setSystem("http://highmed.org/sid/research-study-identifier")
				.setValue(rs.getIdentifierFirstRep().getValue());
		binary.setSecurityContext(securityContext);

		Binary created = getWebserviceClient().create(binary);
		assertNotNull(created);
	}

	@Test
	public void testCreateViaTransactionBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNotNull(responseBundle.getEntry().get(0).getResource());
		assertTrue(responseBundle.getEntry().get(0).getResource() instanceof Binary);
		assertNull(responseBundle.getEntry().get(0).getResponse().getOutcome());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("201 Created", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"1\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test
	public void testCreateViaTransactionBundleWithMinimalReturn() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		Bundle responseBundle = getWebserviceClient().withMinimalReturn().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNull(responseBundle.getEntry().get(0).getResponse().getOutcome());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("201 Created", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"1\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test
	public void testCreateViaTransactionBundleWithOperationOutcomeReturn() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		Bundle responseBundle = getWebserviceClient().withOperationOutcomeReturn().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getOutcome());
		assertTrue(responseBundle.getEntry().get(0).getResponse().getOutcome() instanceof OperationOutcome);
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("201 Created", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"1\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test
	public void testCreateNotAllowedExternalUserViaTransactionBundle() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		expectForbidden(() -> getExternalWebserviceClient().postBundle(bundle));
	}

	@Test
	public void testCreateSecurityContextNotExistingViaTransactionBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference("ResearchStudy/" + UUID.randomUUID().toString()));

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		expectForbidden(() -> getWebserviceClient().create(binary));
	}

	@Test
	public void testCreateViaBatchBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		getReadAccessHelper().addLocal(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("201 Created", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"1\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test
	public void testCreateNotAllowedExternalUserViaBatchBundle() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("403 Forbidden", responseBundle.getEntry().get(0).getResponse().getStatus());
	}

	@Test
	public void testCreateSecurityContextNotExistingViaBatchBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference("ResearchStudy/" + UUID.randomUUID().toString()));

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("403 Forbidden", responseBundle.getEntry().get(0).getResponse().getStatus());
	}

	@Test
	public void testUpdate() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		created.setData(data2);

		Binary updated = getWebserviceClient().update(created);

		assertNotNull(updated);
		assertNotNull(updated.getIdElement().toString());
		assertEquals("2", updated.getMeta().getVersionId());
		assertNotNull(updated.getMeta().getLastUpdated());

		assertNotNull(updated.getContentType());
		assertEquals(contentType, updated.getContentType());
		assertTrue(Arrays.equals(data2, updated.getData()));
	}

	@Test
	public void testUpdateReturnMinimal() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		created.setData(data2);

		IdType updated = getWebserviceClient().withMinimalReturn().update(created);

		assertNotNull(updated);
		assertNotNull(updated.getBaseUrl());
		assertNotNull(updated.getResourceType());
		assertNotNull(updated.getIdPart());
		assertNotNull(updated.getVersionIdPart());

		assertEquals(BASE_URL, updated.getBaseUrl());
		assertEquals("Binary", updated.getResourceType());
		assertEquals("2", updated.getVersionIdPart());
	}

	@Test
	public void testUpdateReturnOperationOutcome() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		created.setData(data2);

		OperationOutcome updated = getWebserviceClient().withOperationOutcomeReturn().update(created);

		assertNotNull(updated);
	}

	@Test
	public void testUpdateNotAllowedExternalUser() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		expectForbidden(() -> getExternalWebserviceClient().update(created));
	}

	@Test
	public void testUpdateViaTransactionBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		created.setData(data2);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(BASE_URL + "/Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNotNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("200 OK", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"2\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test
	public void testUpdateNotAllowedExternalUserViaTransactionBundle() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(BASE_URL + "/Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		expectForbidden(() -> getExternalWebserviceClient().postBundle(bundle));
	}

	@Test
	public void testUpdateSecurityContextNotExistingViaTransactionBundle() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);
		created.setSecurityContext(new Reference("ResearchStudy/" + UUID.randomUUID().toString()));

		assertNotNull(created.getSecurityContext());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(BASE_URL + "/Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		expectForbidden(() -> getWebserviceClient().postBundle(bundle));
	}

	@Test
	public void testUpdateViaBatchBundle() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		created.setData(data2);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl(BASE_URL + "/Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("200 OK", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"2\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test
	public void testUpdateNotAllowedExternalUserViaBatchBundle() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl(BASE_URL + "/Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getExternalWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("403 Forbidden", responseBundle.getEntry().get(0).getResponse().getStatus());
	}

	@Test
	public void testUpdateSecurityContextOrgNotExistingViaBatchBundle() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs = new ResearchStudy();
		getReadAccessHelper().addLocal(rs);

		ResearchStudy createdRs = researchStudyDao.create(rs);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdRs.getIdElement().toVersionless()));

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);
		created.setSecurityContext(new Reference("ResearchStudy/" + UUID.randomUUID().toString()));

		assertNotNull(created.getSecurityContext());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl(BASE_URL + "/Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);
		assertNotNull(responseBundle);
		assertEquals(BundleType.BATCHRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNull(responseBundle.getEntry().get(0).getResource());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("403 Forbidden", responseBundle.getEntry().get(0).getResponse().getStatus());
	}

	@Test
	public void testSearchAll() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs1 = new ResearchStudy();
		getReadAccessHelper().addLocal(rs1);
		ResearchStudy rs2 = new ResearchStudy();
		getReadAccessHelper().addLocal(rs2);
		getReadAccessHelper().addRole(rs2, "Parent_Organization",
				"http://highmed.org/fhir/CodeSystem/organization-type", "MeDIC");

		ResearchStudy createdRs1 = researchStudyDao.create(rs1);
		ResearchStudy createdRs2 = researchStudyDao.create(rs2);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary b1 = new Binary();
		b1.setContentType(contentType);
		b1.setData(data);
		b1.setSecurityContext(new Reference(createdRs1.getIdElement().toVersionless()));

		Binary b2 = new Binary();
		b2.setContentType(contentType);
		b2.setData(data);
		b2.setSecurityContext(new Reference(createdRs2.getIdElement().toVersionless()));

		Binary b3 = new Binary();
		b3.setContentType(contentType);
		b3.setData(data);
		getReadAccessHelper().addLocal(b3);

		Binary b4 = new Binary();
		b4.setContentType(contentType);
		b4.setData(data);
		getReadAccessHelper().addAll(b4);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		b1 = binaryDao.create(b1);
		b2 = binaryDao.create(b2);
		b3 = binaryDao.create(b3);
		b4 = binaryDao.create(b4);

		Bundle searchBundle = getWebserviceClient().search(Binary.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(4, searchBundle.getTotal());
		assertTrue(searchBundle.getEntry().stream()
				.allMatch(c -> c.getResource() != null && c.getResource() instanceof Binary));

		String actualIds = searchBundle.getEntry().stream().map(c -> c.getResource())
				.map(r -> "Binary/" + r.getIdElement().getIdPart() + "/_history/" + r.getMeta().getVersionId()).sorted()
				.collect(Collectors.joining(", "));
		String expectedIds = Arrays.asList(b1, b2, b3, b4).stream().map(b -> b.getIdElement().getValueAsString())
				.sorted().collect(Collectors.joining(", "));
		assertEquals(expectedIds, actualIds);
	}

	@Test
	public void testSearchAllExternalUser() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs1 = new ResearchStudy();
		getReadAccessHelper().addLocal(rs1);

		ResearchStudy rs2 = new ResearchStudy();
		getReadAccessHelper().addLocal(rs2);
		getReadAccessHelper().addOrganization(rs2, "External_Test_Organization");

		ResearchStudy rs3 = new ResearchStudy();
		getReadAccessHelper().addLocal(rs3);
		getReadAccessHelper().addRole(rs3, "Parent_Organization",
				"http://highmed.org/fhir/CodeSystem/organization-type", "MeDIC");

		ResearchStudy createdRs1 = researchStudyDao.create(rs1);
		ResearchStudy createdRs2 = researchStudyDao.create(rs2);
		ResearchStudy createdRs3 = researchStudyDao.create(rs3);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary b1 = new Binary();
		b1.setContentType(contentType);
		b1.setData(data);
		b1.setSecurityContext(new Reference(createdRs1.getIdElement().toVersionless()));

		Binary b2 = new Binary();
		b2.setContentType(contentType);
		b2.setData(data);
		b2.setSecurityContext(new Reference(createdRs2.getIdElement().toVersionless()));

		Binary b3 = new Binary();
		b3.setContentType(contentType);
		b3.setData(data);
		b3.setSecurityContext(new Reference(createdRs3.getIdElement().toVersionless()));

		Binary b4 = new Binary();
		b4.setContentType(contentType);
		b4.setData(data);
		getReadAccessHelper().addLocal(b4);

		Binary b5 = new Binary();
		b5.setContentType(contentType);
		b5.setData(data);
		getReadAccessHelper().addAll(b5);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		b1 = binaryDao.create(b1);
		b2 = binaryDao.create(b2);
		b3 = binaryDao.create(b3);
		b4 = binaryDao.create(b4);
		b5 = binaryDao.create(b5);

		Bundle searchBundle = getExternalWebserviceClient().search(Binary.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(3, searchBundle.getTotal());
		assertTrue(searchBundle.getEntry().stream()
				.allMatch(c -> c.getResource() != null && c.getResource() instanceof Binary));

		String actualIds = searchBundle.getEntry().stream().map(c -> c.getResource())
				.map(r -> "Binary/" + r.getIdElement().getIdPart() + "/_history/" + r.getMeta().getVersionId()).sorted()
				.collect(Collectors.joining(", "));
		String expectedIds = Arrays.asList(b2, b3, b5).stream().map(b -> b.getIdElement().getValueAsString()).sorted()
				.collect(Collectors.joining(", "));
		assertEquals(expectedIds, actualIds);
	}

	@Test(expected = ResourceDeletedException.class)
	public void testDelete() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		getWebserviceClient().delete(Binary.class, created.getIdElement().getIdPart());

		binaryDao.read(UUID.fromString(created.getIdElement().getIdPart()));
	}

	@Test(expected = ResourceDeletedException.class)
	public void testDeleteDeleted() throws Exception
	{
		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		getReadAccessHelper().addLocal(binary);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		boolean deleted = binaryDao.delete(UUID.fromString(created.getIdElement().getIdPart()));
		assertTrue(deleted);

		getWebserviceClient().delete(Binary.class, created.getIdElement().getIdPart());

		binaryDao.read(UUID.fromString(created.getIdElement().getIdPart()));
	}

	@Test
	public void testDeleteNotAllowedExternalUser() throws Exception
	{
		ResearchStudyDao researchStudyDao = getSpringWebApplicationContext().getBean(ResearchStudyDao.class);
		ResearchStudy rs1 = new ResearchStudy();
		getReadAccessHelper().addAll(rs1);

		ResearchStudy createdRs1 = researchStudyDao.create(rs1);

		final String contentType = MediaType.TEXT_PLAIN;
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdRs1.getIdElement().toVersionless()));

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data1, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdRs1.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		expectForbidden(() -> getExternalWebserviceClient().delete(Binary.class, created.getIdElement().getIdPart()));
	}

	@Test
	public void testDeletePermanentlyByLocalDeletionUser() throws Exception
	{
		Binary binary = new Binary();
		readAccessHelper.addLocal(binary);
		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		String binaryId = binaryDao.create(binary).getIdElement().getIdPart();
		binaryDao.delete(UUID.fromString(binaryId));

		getWebserviceClient().deletePermanently(Binary.class, binaryId);

		Optional<Binary> result = binaryDao.read(UUID.fromString(binaryId));
		assertTrue(result.isEmpty());
	}

	@Test
	public void testDeletePermanentlyByLocalDeletionUserNotMarkedAsDeleted() throws Exception
	{
		Binary binary = new Binary();
		readAccessHelper.addLocal(binary);
		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		String binaryId = binaryDao.create(binary).getIdElement().getIdPart();

		expectBadRequest(() -> getWebserviceClient().deletePermanently(Binary.class, binaryId));
	}

	@Test
	public void testDeletePermanentlyByExternalUser() throws Exception
	{
		Binary binary = new Binary();
		readAccessHelper.addLocal(binary);
		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		String binaryId = binaryDao.create(binary).getIdElement().getIdPart();

		expectForbidden(() -> getExternalWebserviceClient().deletePermanently(Binary.class, binaryId));
	}
}
