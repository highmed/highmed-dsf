package org.highmed.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.jdbc.EndpointDaoJdbc;
import org.hl7.fhir.r4.model.Endpoint;

import ca.uhn.fhir.context.FhirContext;

public class EndpointDaoTest extends AbstractDomainResourceDaoTest<Endpoint, EndpointDao>
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
}
