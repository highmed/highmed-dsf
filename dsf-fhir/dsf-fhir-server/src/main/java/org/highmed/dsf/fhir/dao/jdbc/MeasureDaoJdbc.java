package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.MeasureDao;
import org.highmed.dsf.fhir.search.parameters.MeasureDate;
import org.highmed.dsf.fhir.search.parameters.MeasureDependsOn;
import org.highmed.dsf.fhir.search.parameters.MeasureIdentifier;
import org.highmed.dsf.fhir.search.parameters.MeasureStatus;
import org.highmed.dsf.fhir.search.parameters.MeasureUrl;
import org.highmed.dsf.fhir.search.parameters.MeasureVersion;
import org.highmed.dsf.fhir.search.parameters.user.MeasureUserFilter;
import org.hl7.fhir.r4.model.Measure;

import ca.uhn.fhir.context.FhirContext;

public class MeasureDaoJdbc extends AbstractResourceDaoJdbc<Measure> implements MeasureDao
{
	private final ReadByUrlDaoJdbc<Measure> readByUrl;

	public MeasureDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, Measure.class, "measures", "measure", "measure_id",
				MeasureUserFilter::new, with(MeasureDate::new, MeasureDependsOn::new, MeasureIdentifier::new,
						MeasureStatus::new, MeasureUrl::new, MeasureVersion::new),
				with());

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn());
	}

	@Override
	protected Measure copy(Measure resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<Measure> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<Measure> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<Measure> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<Measure> readByUrlAndVersionWithTransaction(Connection connection, String url, String version)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}
}
