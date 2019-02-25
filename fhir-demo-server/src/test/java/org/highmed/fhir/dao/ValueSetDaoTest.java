package org.highmed.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetDaoTest extends AbstractDomainResourceDaoTest<ValueSet, ValueSetDao>
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
		return new ValueSetDao(dataSource, fhirContext);
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
}
