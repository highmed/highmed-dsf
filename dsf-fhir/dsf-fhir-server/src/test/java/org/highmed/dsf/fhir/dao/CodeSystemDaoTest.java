package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.CodeSystemDaoJdbc;
import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;

public class CodeSystemDaoTest extends AbstractResourceDaoTest<CodeSystem, CodeSystemDao>
{
	private static final String name = "Demo CodeSystem Name";
	private static final String description = "Demo CodeSystem Description";

	public CodeSystemDaoTest()
	{
		super(CodeSystem.class);
	}

	@Override
	protected CodeSystemDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new CodeSystemDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected CodeSystem createResource()
	{
		CodeSystem codeSystem = new CodeSystem();
		codeSystem.setName(name);
		return codeSystem;
	}

	@Override
	protected void checkCreated(CodeSystem resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected CodeSystem updateResource(CodeSystem resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(CodeSystem resource)
	{
		assertEquals(description, resource.getDescription());
	}
}
