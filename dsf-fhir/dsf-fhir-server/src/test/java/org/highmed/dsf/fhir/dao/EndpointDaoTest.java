package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.EndpointDaoJdbc;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class EndpointDaoTest extends AbstractResourceDaoTest<Endpoint, EndpointDao>
{
	private static final String name = "Demo Endpoint Name";
	private static final String address = "https://foo.bar/baz";

	public EndpointDaoTest()
	{
		super(Endpoint.class);
	}

	@Override
	protected EndpointDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new EndpointDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Endpoint createResource()
	{
		Endpoint endpoint = new Endpoint();
		endpoint.setName(name);
		return endpoint;
	}

	@Override
	protected void checkCreated(Endpoint resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Endpoint updateResource(Endpoint resource)
	{
		resource.setAddress(address);
		return resource;
	}

	@Override
	protected void checkUpdates(Endpoint resource)
	{
		assertEquals(address, resource.getAddress());
	}

	@Test
	public void testExistsActiveNotDeletedByAddress() throws Exception
	{
		String address = "http://test/fhir";

		Endpoint e = new Endpoint();
		e.setStatus(EndpointStatus.ACTIVE);
		e.setAddress(address);

		Endpoint created = dao.create(e);
		assertNotNull(created);

		assertTrue(dao.existsActiveNotDeletedByAddress(address));
	}

	@Test
	public void testExistsActiveNotDeletedByAddressNotActive() throws Exception
	{
		String address = "http://test/fhir";

		Endpoint e = new Endpoint();
		e.setStatus(EndpointStatus.OFF);
		e.setAddress(address);

		Endpoint created = dao.create(e);
		assertNotNull(created);

		assertFalse(dao.existsActiveNotDeletedByAddress(address));
	}

	@Test
	public void testExistsActiveNotDeletedByAddressDeleted() throws Exception
	{
		String address = "http://test/fhir";

		Endpoint e = new Endpoint();
		e.setStatus(EndpointStatus.ACTIVE);
		e.setAddress(address);

		Endpoint created = dao.create(e);
		assertNotNull(created);
		dao.delete(UUID.fromString(created.getIdElement().getIdPart()));

		assertFalse(dao.existsActiveNotDeletedByAddress(address));
	}
}
