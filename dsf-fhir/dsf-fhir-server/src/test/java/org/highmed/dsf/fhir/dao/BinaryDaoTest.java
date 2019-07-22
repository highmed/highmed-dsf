package org.highmed.dsf.fhir.dao;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.BinaryDaoJdbc;
import org.hl7.fhir.r4.model.Binary;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;
import static ch.qos.logback.core.encoder.ByteArrayUtil.toHexString;
import static org.junit.Assert.assertEquals;

public class BinaryDaoTest extends AbstractDomainResourceDaoTest<Binary, BinaryDao>
{
	private static final String  CONTENT_TYPE = "application/pdf";
	private static final String HEX_DATA = "VBERi0xLjUNJeLjz9MNCjEwIDAgb2JqDTw8L0xpbmVhcml6ZWQgMS9MIDEzMDA2OC9PIDEyL0UgMTI1NzM1L04gMS9UIDEyOTc2NC9IIFsgNTQ2IDIwNF";

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
		return binary;
	}

	@Override
	protected void checkCreated(Binary resource)
	{
		assertEquals(CONTENT_TYPE, resource.getContentType());
	}

	@Override
	protected Binary updateResource(Binary resource)
	{
		resource.setContent(hexStringToByteArray(HEX_DATA));
		return resource;
	}

	@Override
	protected void checkUpdates(Binary resource)
	{
		assertEquals(HEX_DATA, toHexString(resource.getContent()));
	}
}
