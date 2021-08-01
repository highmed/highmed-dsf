package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.search.parameters.ValueSetDate;
import org.highmed.dsf.fhir.search.parameters.ValueSetIdentifier;
import org.highmed.dsf.fhir.search.parameters.ValueSetStatus;
import org.highmed.dsf.fhir.search.parameters.ValueSetUrl;
import org.highmed.dsf.fhir.search.parameters.ValueSetVersion;
import org.highmed.dsf.fhir.search.parameters.user.ValueSetUserFilter;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetDaoJdbc extends AbstractResourceDaoJdbc<ValueSet> implements ValueSetDao
{
	private final ReadByUrlDaoJdbc<ValueSet> readByUrl;

	public ValueSetDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, ValueSet.class, "value_sets", "value_set",
				"value_set_id", ValueSetUserFilter::new, with(ValueSetDate::new, ValueSetIdentifier::new,
						ValueSetStatus::new, ValueSetUrl::new, ValueSetVersion::new),
				with());

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn());
	}

	@Override
	protected ValueSet copy(ValueSet resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<ValueSet> readByUrlAndVersionWithTransaction(Connection connection, String url, String version)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}
}
