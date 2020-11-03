package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.highmed.dsf.fhir.dao.jdbc.NamingSystemDaoJdbc;
import org.hl7.fhir.r4.model.NamingSystem;
import org.junit.Test;

public class NamingSystemDaoTest extends AbstractResourceDaoTest<NamingSystem, NamingSystemDao>
{
	private static final String name = "Demo NamingSystem Name";
	private static final String description = "Demo NamingSystem Description";

	public NamingSystemDaoTest()
	{
		super(NamingSystem.class, NamingSystemDaoJdbc::new);
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

	@Test
	public void testReadByName() throws Exception
	{
		NamingSystem newResource = createResource();
		dao.create(newResource);

		Optional<NamingSystem> readByName = dao.readByName(name);
		assertTrue(readByName.isPresent());
	}
}
