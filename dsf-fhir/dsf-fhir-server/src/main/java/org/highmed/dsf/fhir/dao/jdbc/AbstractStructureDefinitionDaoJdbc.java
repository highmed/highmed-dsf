package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.StructureDefinitionDao;
import org.highmed.dsf.fhir.search.SearchQueryUserFilter;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionDate;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionIdentifier;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionStatus;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionUrl;
import org.highmed.dsf.fhir.search.parameters.StructureDefinitionVersion;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

abstract class AbstractStructureDefinitionDaoJdbc extends AbstractResourceDaoJdbc<StructureDefinition>
		implements StructureDefinitionDao
{
	private final ReadByUrlDaoJdbc<StructureDefinition> readByUrl;

	public AbstractStructureDefinitionDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource,
			FhirContext fhirContext, String resourceTable, String resourceColumn, String resourceIdColumn,
			Function<User, SearchQueryUserFilter> userFilter)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, StructureDefinition.class, resourceTable,
				resourceColumn, resourceIdColumn, userFilter,
				with(() -> new StructureDefinitionDate(resourceColumn),
						() -> new StructureDefinitionIdentifier(resourceColumn),
						() -> new StructureDefinitionStatus(resourceColumn),
						() -> new StructureDefinitionUrl(resourceColumn),
						() -> new StructureDefinitionVersion(resourceColumn)),
				with());

		readByUrl = new ReadByUrlDaoJdbc<StructureDefinition>(this::getDataSource, this::getResource, resourceTable,
				resourceColumn);
	}

	@Override
	public Optional<StructureDefinition> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<StructureDefinition> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<StructureDefinition> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<StructureDefinition> readByUrlAndVersionWithTransaction(Connection connection, String url,
			String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}
}
