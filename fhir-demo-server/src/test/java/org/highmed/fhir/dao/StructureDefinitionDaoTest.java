package org.highmed.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionDaoTest extends AbstractDomainResourceDaoTest<StructureDefinition, StructureDefinitionDao>
{
	private static final String name = "StructureDefinition";
	private static final String title = "Demo Structure Definition";

	public StructureDefinitionDaoTest()
	{
		super(StructureDefinition.class);
	}

	@Override
	protected StructureDefinitionDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new StructureDefinitionDao(dataSource, fhirContext);
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
}
