package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.search.parameters.user.StructureDefinitionSnapshotUserFilter;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionSnapshotDaoJdbc extends AbstractStructureDefinitionDaoJdbc
{
	public StructureDefinitionSnapshotDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, "structure_definition_snapshots",
				"structure_definition_snapshot", "structure_definition_snapshot_id",
				StructureDefinitionSnapshotUserFilter::new);
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}
}
