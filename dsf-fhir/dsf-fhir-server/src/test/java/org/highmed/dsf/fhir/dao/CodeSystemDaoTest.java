package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.CodeSystemDaoJdbc;
import org.hl7.fhir.r4.model.CodeSystem;
import org.junit.Test;

public class CodeSystemDaoTest extends AbstractResourceDaoTest<CodeSystem, CodeSystemDao>
		implements ReadByUrlDaoTest<CodeSystem>
{
	private static final String name = "Demo CodeSystem Name";
	private static final String description = "Demo CodeSystem Description";

	public CodeSystemDaoTest()
	{
		super(CodeSystem.class, CodeSystemDaoJdbc::new);
	}

	@Override
	protected CodeSystem createResource()
	{
		CodeSystem codeSystem = new CodeSystem();
		codeSystem.setName(name);
		return codeSystem;
	}

	@Override
	protected void checkCreated(CodeSystem resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected CodeSystem updateResource(CodeSystem resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(CodeSystem resource)
	{
		assertEquals(description, resource.getDescription());
	}

	@Override
	public CodeSystem createResourceWithUrlAndVersion()
	{
		CodeSystem resource = createResource();
		resource.setUrl(getUrl());
		resource.setVersion(getVersion());
		return resource;
	}

	@Override
	public String getUrl()
	{
		return "http://test.com/fhir/CodeSystem/test-system";
	}

	@Override
	public String getVersion()
	{
		return "0.2.0";
	}

	@Override
	public ReadByUrlDao<CodeSystem> readByUrlDao()
	{
		return getDao();
	}

	@Override
	public ResourceDao<CodeSystem> dao()
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
