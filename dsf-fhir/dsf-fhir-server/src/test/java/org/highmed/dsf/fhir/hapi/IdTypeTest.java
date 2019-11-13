package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.*;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.junit.Test;

public class IdTypeTest
{
	@Test
	public void testIdNull() throws Exception
	{
		Task task = new Task();

		assertTrue(task.getId() == null);

		assertFalse(task.hasId());
		assertFalse(task.hasIdElement());

		assertTrue(task.getIdElement() != null);

		assertFalse(task.hasId());
		assertFalse(task.hasIdElement()); // <- this should be true

		assertTrue(task.getIdElement().getValue() == null);
		assertTrue(task.getIdElement().getIdPart() == null);
		assertTrue(task.getIdElement().getVersionIdPart() == null);
		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdValue() throws Exception
	{
		final String idValue = "value";

		Task task = new Task();
		task.setId(idValue);

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idValue, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idValue, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() == null);
		assertTrue(task.getIdElement().getVersionIdPart() == null);
		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdElementValue() throws Exception
	{
		final String idValue = "value";

		Task task = new Task();
		task.setIdElement(new IdType(idValue));

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idValue, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idValue, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() == null);
		assertTrue(task.getIdElement().getVersionIdPart() == null);
		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdResourceTypeAndValue() throws Exception
	{
		final String idValue = "value";
		final String idResourceType = "Task";

		Task task = new Task();
		task.setId(idResourceType + "/" + idValue);

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idResourceType + "/" + idValue, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idResourceType + "/" + idValue, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() == null);
		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdElementResourceTypeAndValue1() throws Exception
	{
		final String idValue = "value";
		final String idResourceType = "Task";

		Task task = new Task();
		task.setIdElement(new IdType(idResourceType + "/" + idValue));

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idResourceType + "/" + idValue, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idResourceType + "/" + idValue, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() == null);
		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdElementResourceTypeAndValue2() throws Exception
	{
		final String idValue = "value";
		final String idResourceType = "Task";

		Task task = new Task();
		task.setIdElement(new IdType(idResourceType, idValue));

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idResourceType + "/" + idValue, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idResourceType + "/" + idValue, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() == null);
		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdResourceTypeAndValueAndVersion() throws Exception
	{
		final String idValue = "value";
		final String idResourceType = "Task";
		final String idVersion = "version";

		Task task = new Task();
		task.setId(idResourceType + "/" + idValue + "/_history/" + idVersion);

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idResourceType + "/" + idValue + "/_history/" + idVersion, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idResourceType + "/" + idValue + "/_history/" + idVersion, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() != null);
		assertEquals(idVersion, task.getIdElement().getVersionIdPart());

		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdElementResourceTypeAndValueAndVersion1() throws Exception
	{
		final String idValue = "value";
		final String idResourceType = "Task";
		final String idVersion = "version";

		Task task = new Task();
		task.setIdElement(new IdType(idResourceType + "/" + idValue + "/_history/" + idVersion));

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idResourceType + "/" + idValue + "/_history/" + idVersion, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idResourceType + "/" + idValue + "/_history/" + idVersion, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() != null);
		assertEquals(idVersion, task.getIdElement().getVersionIdPart());

		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdElementResourceTypeAndValueAnVersion2() throws Exception
	{
		final String idValue = "value";
		final String idResourceType = "Task";
		final String idVersion = "version";

		Task task = new Task();
		task.setIdElement(new IdType(idResourceType, idValue, idVersion));

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idResourceType + "/" + idValue + "/_history/" + idVersion, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idResourceType + "/" + idValue + "/_history/" + idVersion, task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() != null);
		assertEquals(idVersion, task.getIdElement().getVersionIdPart());

		assertTrue(task.getIdElement().getBaseUrl() == null);
	}

	@Test
	public void testSetIdBaseUrlResourceTypeAndValueAndVersion() throws Exception
	{
		final String idBaseUrl = "http://test.com";
		final String idValue = "value";
		final String idResourceType = "Task";
		final String idVersion = "version";

		Task task = new Task();
		task.setId(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion);

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion,
				task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() != null);
		assertEquals(idVersion, task.getIdElement().getVersionIdPart());

		assertTrue(task.getIdElement().getBaseUrl() != null);
		assertEquals(idBaseUrl, task.getIdElement().getBaseUrl());
	}

	@Test
	public void testSetIdElementBaseUrlResourceTypeAndValueAndVersion1() throws Exception
	{
		final String idBaseUrl = "http://test.com";
		final String idValue = "value";
		final String idResourceType = "Task";
		final String idVersion = "version";

		Task task = new Task();
		task.setIdElement(new IdType(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion));

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion,
				task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() != null);
		assertEquals(idVersion, task.getIdElement().getVersionIdPart());

		assertTrue(task.getIdElement().getBaseUrl() != null);
		assertEquals(idBaseUrl, task.getIdElement().getBaseUrl());
	}

	@Test
	public void testSetIdElementBaseUrlResourceTypeAndValueAnVersion2() throws Exception
	{
		final String idBaseUrl = "http://test.com";
		final String idValue = "value";
		final String idResourceType = "Task";
		final String idVersion = "version";

		Task task = new Task();
		task.setIdElement(new IdType(idBaseUrl, idResourceType, idValue, idVersion));

		assertTrue(task.hasId());
		assertTrue(task.hasIdElement());

		assertTrue(task.getId() != null);
		assertEquals(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion, task.getId());

		assertTrue(task.getIdElement() != null);

		assertTrue(task.getIdElement().getValue() != null);
		assertEquals(idBaseUrl + "/" + idResourceType + "/" + idValue + "/_history/" + idVersion,
				task.getIdElement().getValue());

		assertTrue(task.getIdElement().getIdPart() != null);
		assertEquals(idValue, task.getIdElement().getIdPart());

		assertTrue(task.getIdElement().getResourceType() != null);
		assertEquals(idResourceType, task.getIdElement().getResourceType());

		assertTrue(task.getIdElement().getVersionIdPart() != null);
		assertEquals(idVersion, task.getIdElement().getVersionIdPart());

		assertTrue(task.getIdElement().getBaseUrl() != null);
		assertEquals(idBaseUrl, task.getIdElement().getBaseUrl());
	}
}
