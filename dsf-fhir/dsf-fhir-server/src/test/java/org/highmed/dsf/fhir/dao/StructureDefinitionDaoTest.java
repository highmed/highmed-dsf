package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.StructureDefinitionDaoJdbc;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.Test;

public class StructureDefinitionDaoTest extends AbstractResourceDaoTest<StructureDefinition, StructureDefinitionDao>
		implements ReadByUrlDaoTest<StructureDefinition>
{
	private static final String name = "StructureDefinition";
	private static final String title = "Demo Structure Definition";

	public StructureDefinitionDaoTest()
	{
		super(StructureDefinition.class, StructureDefinitionDaoJdbc::new);
	}

	@Override
	protected StructureDefinition createResource()
	{
		StructureDefinition structureDefinition = new StructureDefinition();
		structureDefinition.setName(name);
		return structureDefinition;
	}

	@Override
	protected void checkCreated(StructureDefinition resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected StructureDefinition updateResource(StructureDefinition resource)
	{
		resource.setTitle(title);
		return resource;
	}

	@Override
	protected void checkUpdates(StructureDefinition resource)
	{
		assertEquals(title, resource.getTitle());
	}

	@Override
	public StructureDefinition createResourceWithUrlAndVersion()
	{
		StructureDefinition resource = createResource();
		resource.setUrl(getUrl());
		resource.setVersion(getVersion());
		return resource;
	}

	@Override
	public String getUrl()
	{
		return "http://test.com/fhir/StructureDefinition/test-system";
	}

	@Override
	public String getVersion()
	{
		return "0.2.0";
	}

	@Override
	public ReadByUrlDao<StructureDefinition> readByUrlDao()
	{
		return getDao();
	}

	@Override
	public ResourceDao<StructureDefinition> dao()
	{
		return getDao();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrl1() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrl1();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrlAndVersion1() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrlAndVersion1();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrl2() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrl2();
	}

	@Override
	@Test
	public void testReadByUrlAndVersionWithUrlAndVersion2() throws Exception
	{
		ReadByUrlDaoTest.super.testReadByUrlAndVersionWithUrlAndVersion2();
	}
}
