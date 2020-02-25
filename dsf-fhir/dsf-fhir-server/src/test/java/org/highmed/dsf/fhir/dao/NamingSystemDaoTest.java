package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.NamingSystemDaoJdbc;
import org.hl7.fhir.r4.model.NamingSystem;

import ca.uhn.fhir.context.FhirContext;

public class NamingSystemDaoTest extends AbstractResourceDaoTest<NamingSystem, NamingSystemDao>
{
	private static final String name = "Demo NamingSystem Name";
	private static final String description = "Demo NamingSystem Description";

	public NamingSystemDaoTest()
	{
		super(NamingSystem.class);
	}

	@Override
	protected NamingSystemDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new NamingSystemDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected NamingSystem createResource()
	{
		NamingSystem namingSystem = new NamingSystem();
		namingSystem.setName(name);
		return namingSystem;
	}

	@Override
	protected void checkCreated(NamingSystem resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected NamingSystem updateResource(NamingSystem resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(NamingSystem resource)
	{
		assertEquals(description, resource.getDescription());
	}
}
