package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.ValueSetDaoJdbc;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetDaoTest extends AbstractResourceDaoTest<ValueSet, ValueSetDao>
		implements ReadByUrlDaoTest<ValueSet>
{
	private static final String name = "Demo ValueSet Name";
	private static final String description = "Demo ValueSet Description";

	public ValueSetDaoTest()
	{
		super(ValueSet.class);
	}

	@Override
	protected ValueSetDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new ValueSetDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected ValueSet createResource()
	{
		ValueSet valueSet = new ValueSet();
		valueSet.setName(name);
		return valueSet;
	}

	@Override
	protected void checkCreated(ValueSet resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected ValueSet updateResource(ValueSet resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(ValueSet resource)
	{
		assertEquals(description, resource.getDescription());
	}

	@Override
	public ValueSet createResourceWithUrlAndVersion()
	{
		ValueSet resource = createResource();
		resource.setUrl(getUrl());
		resource.setVersion(getVersion());
		return resource;
	}

	@Override
	public String getUrl()
	{
		return "http://test.com/fhir/ValueSet/test-system";
	}

	@Override
	public String getVersion()
	{
		return "0.2.0";
	}

	@Override
	public ReadByUrlDao<ValueSet> readByUrlDao()
	{
		return getDao();
	}

	@Override
	public ResourceDao<ValueSet> dao()
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
