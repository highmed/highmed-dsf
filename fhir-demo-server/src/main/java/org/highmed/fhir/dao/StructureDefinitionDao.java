package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.StructureDefinitionUrl;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

public class StructureDefinitionDao extends AbstractDomainResourceDao<StructureDefinition>
{
	public StructureDefinitionDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, StructureDefinition.class, "structure_definitions", "structure_definition",
				"structure_definition_id", StructureDefinitionUrl::new);
	}

	@Override
	protected StructureDefinition copy(StructureDefinition resource)
	{
		return resource.copy();
	}
}
