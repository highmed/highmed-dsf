package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.search.parameters.CodeSystemDate;
import org.highmed.dsf.fhir.search.parameters.CodeSystemIdentifier;
import org.highmed.dsf.fhir.search.parameters.CodeSystemStatus;
import org.highmed.dsf.fhir.search.parameters.CodeSystemUrl;
import org.highmed.dsf.fhir.search.parameters.CodeSystemVersion;
import org.highmed.dsf.fhir.search.parameters.user.CodeSystemUserFilter;
import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;

public class CodeSystemDaoJdbc extends AbstractResourceDaoJdbc<CodeSystem> implements CodeSystemDao
{
	private final ReadByUrlDaoJdbc<CodeSystem> readByUrl;

	public CodeSystemDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, CodeSystem.class, "code_systems", "code_system",
				"code_system_id", CodeSystemUserFilter::new, with(CodeSystemDate::new, CodeSystemIdentifier::new,
						CodeSystemStatus::new, CodeSystemUrl::new, CodeSystemVersion::new),
				with());

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn());
	}

	@Override
	protected CodeSystem copy(CodeSystem resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<CodeSystem> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<CodeSystem> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<CodeSystem> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<CodeSystem> readByUrlAndVersionWithTransaction(Connection connection, String url, String version)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}
}
