package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.ActivityDefinitionDaoJdbc;
import org.hl7.fhir.r4.model.ActivityDefinition;

import ca.uhn.fhir.context.FhirContext;

public class ActivityDefinitionDaoTest extends AbstractResourceDaoTest<ActivityDefinition, ActivityDefinitionDao>
{
	private static final String name = "Demo ActivityDefinition Name";
	private static final String title = "Demo ActivityDefinition Title";

	public ActivityDefinitionDaoTest()
	{
		super(ActivityDefinition.class);
	}

	@Override
	protected ActivityDefinitionDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new ActivityDefinitionDaoJdbc(dataSource, fhirContext);
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
}
