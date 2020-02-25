package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.GroupDaoJdbc;
import org.hl7.fhir.r4.model.Group;

import ca.uhn.fhir.context.FhirContext;

public class GroupDaoTest extends AbstractResourceDaoTest<Group, GroupDao>
{
	private static final String name = "Demo Group";
	private static final Group.GroupType type = Group.GroupType.PERSON;
	private static final boolean actual = true;

	public GroupDaoTest()
	{
		super(Group.class);
	}

	@Override
	protected GroupDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new GroupDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Group createResource()
	{
		Group group = new Group();
		group.setType(type);
		group.setActual(actual);
		return group;
	}

	@Override
	protected void checkCreated(Group resource)
	{
		assertEquals(type, resource.getType());
		assertEquals(actual, resource.getActual());
	}

	@Override
	protected Group updateResource(Group resource)
	{
		resource.setName(name);
		return resource;
	}

	@Override
	protected void checkUpdates(Group resource)
	{
		assertEquals(name, resource.getName());
	}
}
