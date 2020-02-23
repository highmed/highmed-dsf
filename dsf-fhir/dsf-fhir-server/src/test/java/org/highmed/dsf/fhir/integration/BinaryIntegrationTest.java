package org.highmed.dsf.fhir.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.search.PartialResult;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;

public class BinaryIntegrationTest extends AbstractIntegrationTest
{
	@Test
	public void testReadAllowed() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		PartialResult<Organization> result = orgDao
				.search(orgDao.createSearchQueryWithoutUserFilter(1, 1).configureParameters(Collections.emptyMap()));
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

	@Test(expected = WebApplicationException.class)
	public void testReadNotAllowed() throws Exception
	{
		OrganizationDao orgDao = getSpringWebApplicationContext().getBean(OrganizationDao.class);
		Organization createdOrg = orgDao.create(new Organization());

		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);
		binary.setSecurityContext(new Reference(createdOrg.getIdElement().toVersionless()));

		BinaryDao binDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binDao.create(binary);

		try
		{
			getWebserviceClient().read(Binary.class, created.getIdElement().getIdPart());
		}
		catch (WebApplicationException e)
		{
			assertEquals(Status.FORBIDDEN.getStatusCode(), e.getResponse().getStatus());
			throw e;
		}
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

	@Test
	public void testSearchAll() throws Exception
	{
		final String contentType = "text/plain";
		final byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);

		Binary binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(data);

		BinaryDao binaryDao = getSpringWebApplicationContext().getBean(BinaryDao.class);
		Binary created = binaryDao.create(binary);

		assertNotNull(created);
		assertNotNull(created.getIdElement().toString());
		assertEquals("1", created.getMeta().getVersionId());
		assertNotNull(created.getMeta().getLastUpdated());

		assertNotNull(created.getContentType());
		assertEquals(contentType, created.getContentType());
		assertTrue(Arrays.equals(data, created.getData()));

		Bundle searchBundle = getWebserviceClient().search(Binary.class, Collections.emptyMap());
		assertNotNull(searchBundle);
		assertEquals(1, searchBundle.getTotal());
		assertNotNull(searchBundle.getEntry().get(0));
		assertNotNull(searchBundle.getEntry().get(0).getSearch());
		assertEquals(SearchEntryMode.MATCH, searchBundle.getEntry().get(0).getSearch().getMode());
		assertNotNull(searchBundle.getEntry().get(0).getResource());
		assertTrue(searchBundle.getEntry().get(0).getResource() instanceof Binary);

		Binary found = (Binary) searchBundle.getEntry().get(0).getResource();
		assertNotNull(found.getContentType());
		assertEquals(contentType, found.getContentType());
		assertNotNull(found.getData());
		assertTrue(Arrays.equals(data, found.getData()));
	}
}
