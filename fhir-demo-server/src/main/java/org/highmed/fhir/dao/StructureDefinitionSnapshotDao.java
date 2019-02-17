package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionSnapshotDao extends AbstractDomainResourceDao<StructureDefinition>
{
	public StructureDefinitionSnapshotDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, StructureDefinition.class, "structure_definition_snapshots", "structure_definition_snapshot",
				"structure_definition_snapshot_id");
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}
}
