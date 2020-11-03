package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.ActivityDefinitionDaoJdbc;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.junit.Test;

public class ActivityDefinitionDaoTest extends AbstractResourceDaoTest<ActivityDefinition, ActivityDefinitionDao>
		implements ReadByUrlDaoTest<ActivityDefinition>
{
	private static final String name = "Demo ActivityDefinition Name";
	private static final String title = "Demo ActivityDefinition Title";

	public ActivityDefinitionDaoTest()
	{
		super(ActivityDefinition.class, ActivityDefinitionDaoJdbc::new);
	}

	@Override
	protected ActivityDefinition createResource()
	{
		ActivityDefinition activityDefinition = new ActivityDefinition();
		activityDefinition.setName(name);
		return activityDefinition;
	}

	@Override
	protected void checkCreated(ActivityDefinition resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected ActivityDefinition updateResource(ActivityDefinition resource)
	{
		resource.setTitle(title);
		return resource;
	}

	@Override
	protected void checkUpdates(ActivityDefinition resource)
	{
		assertEquals(title, resource.getTitle());
	}

	@Override
	public ActivityDefinition createResourceWithUrlAndVersion()
	{
		ActivityDefinition resource = createResource();
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
		return "0.3.0";
	}

	@Override
	public ReadByUrlDao<ActivityDefinition> readByUrlDao()
	{
		return getDao();
	}

	@Override
	public ResourceDao<ActivityDefinition> dao()
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
