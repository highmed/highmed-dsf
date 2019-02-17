package org.highmed.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionSnapshotDaoTest
		extends AbstractDomainResourceDaoTest<StructureDefinition, StructureDefinitionSnapshotDao>
{
	private static final String name = "StructureDefinitionSnapshot";
	private static final String title = "Demo Structure Definition Snapshot";

	public StructureDefinitionSnapshotDaoTest()
	{
		super(StructureDefinition.class);
	}

	@Override
	protected StructureDefinitionSnapshotDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new StructureDefinitionSnapshotDao(dataSource, fhirContext);
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
