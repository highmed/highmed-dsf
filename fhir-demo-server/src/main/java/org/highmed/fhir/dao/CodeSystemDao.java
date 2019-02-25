package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;

public class CodeSystemDao extends AbstractDomainResourceDao<CodeSystem>
{
	public CodeSystemDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, CodeSystem.class, "code_systems", "code_system", "code_system_id");
	}

	@Override
	protected CodeSystem copy(CodeSystem resource)
	{
		return resource.copy();
	}
}
