package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.search.PartialResult;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;

public class BinaryIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testReadAllowedLocalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		getWebserviceClient().read(Binary.class, created.getIdElement().getIdPart());
	}

	@Test
	public void testReadAllowedExternalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("External Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		getExternalWebserviceClient().read(Binary.class, created.getIdElement().getIdPart());
	}

	@Test(expected = WebApplicationException.class)
	public void testReadNotAllowedExternalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		try
		{
			getExternalWebserviceClient().read(Binary.class, created.getIdElement().getIdPart());
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testReadAllowedLocalUserViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

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
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

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

	@Test(expected = WebApplicationException.class)
	public void testReadNotAllowedExternalUserViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().getRequest().setMethod(HTTPVerb.GET).setUrl("Binary/" + created.getIdElement().getIdPart());

		try
		{
			getExternalWebserviceClient().postBundle(bundle);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testReadAllowedLocalUserViaBatchBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

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
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

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
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao.search(orgDao.createSearchQueryWithoutUserFilter(1, 1)
				.configureParameters(Map.of("name", Arrays.asList("Test Organization"))));
		assertNotNull(result);
		assertEquals(1, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(1, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));

		Organization org = result.getPartialResult().get(0);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(org.getIdElement().toVersionless()));

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
	public void testCreate() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

		Binary created = getWebserviceClient().create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data, created.getData()));

		assertNotNull(created.getSecurityContext());
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());
	}

	@Test(expected = WebApplicationException.class)
	public void testCreateNotAllowedExternalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

		try
		{
			getExternalWebserviceClient().create(binary);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testCreateSecurityContextOrgNotExisting() throws Exception
	{
		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference("Organization/" + UUID.randomUUID().toString()));

		try
		{
			getWebserviceClient().create(binary);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testCreateViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("201 Created", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"1\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test(expected = WebApplicationException.class)
	public void testCreateNotAllowedExternalUserViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		try
		{
			getExternalWebserviceClient().postBundle(bundle);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testCreateSecurityContextOrgNotExistingViaTransactionBundle() throws Exception
	{
		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference("Organization/" + UUID.randomUUID().toString()));

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl("urn:uuid:" + UUID.randomUUID().toString()).setResource(binary).getRequest()
				.setMethod(HTTPVerb.POST).setUrl("Binary");

		try
		{
			getWebserviceClient().create(binary);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testCreateViaBatchBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
	public void testCreateSecurityContextOrgNotExistingViaBatchBundle() throws Exception
	{
		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference("Organization/" + UUID.randomUUID().toString()));

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
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		Binary updated = getWebserviceClient().update(created);

		assertNotNull(updated);
		assertNotNull(updated.getIdElement().toString());
		assertEquals("2", updated.getMeta().getVersionId());
		assertNotNull(updated.getMeta().getLastUpdated());

		assertNotNull(updated.getContentType());
		assertEquals(contentType, updated.getContentType());
		assertTrue(Arrays.equals(data2, updated.getData()));
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateNotAllowedExternalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		try
		{
			getExternalWebserviceClient().update(created);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testUpdateViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(BASE_URL + "Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		Bundle responseBundle = getWebserviceClient().postBundle(bundle);

		assertNotNull(responseBundle);
		assertEquals(BundleType.TRANSACTIONRESPONSE, responseBundle.getType());
		assertEquals(1, responseBundle.getEntry().size());
		assertNotNull(responseBundle.getEntry().get(0));

		assertNotNull(responseBundle.getEntry().get(0).getResponse().getStatus());
		assertEquals("200 OK", responseBundle.getEntry().get(0).getResponse().getStatus());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getLocation());
		assertNotNull(responseBundle.getEntry().get(0).getResponse().getEtag());
		assertEquals("W/\"2\"", responseBundle.getEntry().get(0).getResponse().getEtag());
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateNotAllowedExternalUserViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(BASE_URL + "Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		try
		{
			getExternalWebserviceClient().postBundle(bundle);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testUpdateSecurityContextOrgNotExistingViaTransactionBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);
		created.setSecurityContext(new Reference("Organization/" + UUID.randomUUID().toString()));

		assertNotNull(created.getSecurityContext());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.TRANSACTION);
		bundle.addEntry().setFullUrl(BASE_URL + "Binary/" + created.getIdElement().getIdPart()).setResource(created)
				.getRequest().setMethod(HTTPVerb.PUT).setUrl("Binary/" + created.getIdElement().getIdPart());

		try
		{
			getWebserviceClient().postBundle(bundle);
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}

	@Test
	public void testUpdateViaBatchBundle() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl(BASE_URL + "Binary/" + created.getIdElement().getIdPart()).setResource(created)
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
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);

		assertNotNull(created.getSecurityContext());
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl(BASE_URL + "Binary/" + created.getIdElement().getIdPart()).setResource(created)
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
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);
		final byte[] data2 = "Hello World and goodbye".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		created.setData(data2);
		created.setSecurityContext(new Reference("Organization/" + UUID.randomUUID().toString()));

		assertNotNull(created.getSecurityContext());

		Bundle bundle = new Bundle();
		bundle.setType(BundleType.BATCH);
		bundle.addEntry().setFullUrl(BASE_URL + "Binary/" + created.getIdElement().getIdPart()).setResource(created)
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
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao
				.search(orgDao.createSearchQueryWithoutUserFilter(1, 2).configureParameters(Collections.emptyMap()));
		assertNotNull(result);
		assertEquals(2, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(2, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));
		assertNotNull(result.getPartialResult().get(1));

		Organization org1 = result.getPartialResult().get(0);
		Organization org2 = result.getPartialResult().get(1);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary b1 = new Binary();
		b1.setContentType(contentType);
		b1.setData(data);
		b1.setSecurityContext(new Reference(org1.getIdElement()));
		Binary b2 = new Binary();
		b2.setContentType(contentType);
		b2.setData(data);
		b2.setSecurityContext(new Reference(org1.getIdElement().toVersionless()));
		Binary b3 = new Binary();
		b3.setContentType(contentType);
		b3.setData(data);
		b3.setSecurityContext(new Reference(org2.getIdElement()));
		Binary b4 = new Binary();
		b4.setContentType(contentType);
		b4.setData(data);
		b4.setSecurityContext(new Reference(org2.getIdElement().toVersionless()));

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
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao
				.search(orgDao.createSearchQueryWithoutUserFilter(1, 2).configureParameters(Collections.emptyMap()));
		assertNotNull(result);
		assertEquals(2, result.getOverallCount());
		assertNotNull(result.getPartialResult());
		assertEquals(2, result.getPartialResult().size());
		assertNotNull(result.getPartialResult().get(0));
		assertNotNull(result.getPartialResult().get(1));

		Organization org1 = result.getPartialResult().get(0);
		Organization org2 = result.getPartialResult().get(1);

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary b1 = new Binary();
		b1.setContentType(contentType);
		b1.setData(data);
		b1.setSecurityContext(new Reference(org1.getIdElement()));
		Binary b2 = new Binary();
		b2.setContentType(contentType);
		b2.setData(data);
		b2.setSecurityContext(new Reference(org1.getIdElement().toVersionless()));
		Binary b3 = new Binary();
		b3.setContentType(contentType);
		b3.setData(data);
		b3.setSecurityContext(new Reference(org2.getIdElement()));
		Binary b4 = new Binary();
		b4.setContentType(contentType);
		b4.setData(data);
		b4.setSecurityContext(new Reference(org2.getIdElement().toVersionless()));

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		b1 = binaryDao.create(b1);
		b2 = binaryDao.create(b2);
		b3 = binaryDao.create(b3);
		b4 = binaryDao.create(b4);

		Bundle searchBundle = getExternalWebserviceClient().search(Binary.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(2, searchBundle.getTotal());
		assertTrue(searchBundle.getEntry().stream()
				.allMatch(c -> c.getResource() != null && c.getResource() instanceof Binary));

		String actualIds = searchBundle.getEntry().stream().map(c -> c.getResource())
				.map(r -> "Binary/" + r.getIdElement().getIdPart() + "/_history/" + r.getMeta().getVersionId()).sorted()
				.collect(Collectors.joining(", "));
		List<Binary> binaries = "External Test Organization".equals(org1.getName()) ? Arrays.asList(b1, b2)
				: Arrays.asList(b3, b4);
		String expectedIds = binaries.stream().map(b -> b.getIdElement().getValueAsString()).sorted()
				.collect(Collectors.joining(", "));
		assertEquals(expectedIds, actualIds);
	}

	@Test(expected = ResourceDeletedException.class)
	public void testDelete() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		getWebserviceClient().delete(Binary.class, created.getIdElement().getIdPart());

		binaryDao.read(UUID.fromString(created.getIdElement().getIdPart()));
	}

	@Test(expected = WebApplicationException.class)
	public void testDeleteNotAllowedExternalUser() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data1 = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data1);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

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
		assertEquals(createdOrg.getIdElement().toVersionless(), created.getSecurityContext().getReferenceElement());

		try
		{
			getExternalWebserviceClient().delete(Binary.class, created.getIdElement().getIdPart());
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
	}
}
