package org.highmed.dsf.fhir.dao.jdbc;

import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.search.parameters.ValueSetIdentifier;
import org.highmed.dsf.fhir.search.parameters.ValueSetUrl;
import org.highmed.dsf.fhir.search.parameters.ValueSetVersion;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetDaoJdbc extends AbstractResourceDaoJdbc<ValueSet> implements ValueSetDao
{
	private final ReadByUrlDaoJdbc<ValueSet> readByUrl;

	public ValueSetDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, ValueSet.class, "value_sets", "value_set", "value_set_id",
				ValueSetIdentifier::new, ValueSetUrl::new, ValueSetVersion::new);

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(), getResourceColumn(),
				getResourceIdColumn());
	}

	@Override
	protected ValueSet copy(ValueSet resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<ValueSet> readByUrl(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrl(urlAndVersion);
	}
}
