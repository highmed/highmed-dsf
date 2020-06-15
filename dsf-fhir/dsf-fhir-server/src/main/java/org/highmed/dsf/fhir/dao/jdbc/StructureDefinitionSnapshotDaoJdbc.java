package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.search.parameters.user.StructureDefinitionSnapshotUserFilter;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionSnapshotDaoJdbc extends AbstractStructureDefinitionDaoJdbc
{
	public StructureDefinitionSnapshotDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, "structure_definition_snapshots", "structure_definition_snapshot",
				"structure_definition_snapshot_id", StructureDefinitionSnapshotUserFilter::new);
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}
}
