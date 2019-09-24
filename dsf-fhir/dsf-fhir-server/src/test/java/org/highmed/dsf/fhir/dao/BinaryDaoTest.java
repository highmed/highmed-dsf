package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.hl7.fhir.r4.model.Binary;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class BinaryDaoTest extends AbstractResourceDaoTest<Binary, BinaryDao>
{
	private static final String CONTENT_TYPE = "text/plain";
	private static final byte[] DATA1 = "1234567890".getBytes();
	private static final byte[] DATA2 = "VBERi0xLjUNJeLjz9MNCjEwIDAgb2JqDTw8L0xpbmVhcml6ZWQgMS9MIDEzMDA2OC9PIDEyL0UgMTI1NzM1L04gMS9UIDEyOTc2NC9IIFsgNTQ2IDIwNF"
			.getBytes();

	public BinaryDaoTest()
	{
		super(Binary.class);
	}

	@Override
	protected BinaryDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new BinaryDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Binary createResource()
	{
		Binary binary = new Binary();
		binary.setContentType(CONTENT_TYPE);
		binary.setData(DATA1);
		return binary;
	}

	@Override
	protected void checkCreated(Binary resource)
	{
		assertNotNull(resource.getContentType());
		assertEquals(CONTENT_TYPE, resource.getContentType());
		assertNotNull(resource.getData());
		assertTrue(Arrays.equals(DATA1, resource.getData()));
	}

	@Override
	protected Binary updateResource(Binary resource)
	{
		resource.setData(DATA2);
		return resource;
	}

	@Override
	protected void checkUpdates(Binary resource)
	{
		assertNotNull(resource.getData());
		assertTrue(Arrays.equals(DATA2, resource.getData()));
	}

	@Test
	public void testCreateCheckDataNullInJsonColumn() throws Exception
	{
		Binary newResource = createResource();
		assertNull(newResource.getId());
		assertNull(newResource.getMeta().getVersionId());

		Binary createdResource = dao.create(newResource);
		assertNotNull(createdResource);
		assertNotNull(createdResource.getId());
		assertNotNull(createdResource.getMeta().getVersionId());
		assertEquals("1", createdResource.getIdElement().getVersionIdPart());
		assertEquals("1", createdResource.getMeta().getVersionId());

		try (Connection connection = database.getDataSource().getConnection();
				PreparedStatement statement = connection
						.prepareStatement("SELECT binary_json, binary_data FROM binaries");
				ResultSet result = statement.executeQuery())
		{
			assertTrue(result.next());

			String json = result.getString(1);
			Binary readResource = fhirContext.newJsonParser().parseResource(Binary.class, json);
			assertNotNull(readResource);
			assertNull(readResource.getData());

			byte[] data = result.getBytes(2);
			assertNotNull(data);
			assertTrue(Arrays.equals(DATA1, data));

			assertFalse(result.next());
		}
	}
}
