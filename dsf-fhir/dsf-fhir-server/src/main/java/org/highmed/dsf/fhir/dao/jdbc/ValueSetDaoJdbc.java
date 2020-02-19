package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.ValueSetDao;
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

	public ValueSetDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext, OrganizationType organizationType)
	{
		super(dataSource, fhirContext, ValueSet.class, "value_sets", "value_set", "value_set_id", organizationType,
				ValueSetUserFilter::new,
				with(ValueSetIdentifier::new, ValueSetStatus::new, ValueSetUrl::new, ValueSetVersion::new), with());

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn(), getResourceIdColumn());
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
