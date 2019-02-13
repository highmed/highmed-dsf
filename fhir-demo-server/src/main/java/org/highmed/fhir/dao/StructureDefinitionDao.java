package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionDao extends AbstractDao<StructureDefinition>
{
	public StructureDefinitionDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, StructureDefinition.class, "structure_definitions", "structure_definition",
				"structure_definition_id");
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}

	public PartialResult<StructureDefinition> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
