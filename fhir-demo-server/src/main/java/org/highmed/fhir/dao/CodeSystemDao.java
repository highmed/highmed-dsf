package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;

public class CodeSystemDao extends AbstractDomainResourceDao<CodeSystem>
{
	private final ReadByUrl<CodeSystem> readByUrl;

	public CodeSystemDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, CodeSystem.class, "code_systems", "code_system", "code_system_id");

		readByUrl = new ReadByUrl<CodeSystem>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn(), getResourceIdColumn());
	}

	@Override
	protected CodeSystem copy(CodeSystem resource)
	{
		return resource.copy();
	}

	public Optional<CodeSystem> readByUrl(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrl(urlAndVersion);
	}
}
