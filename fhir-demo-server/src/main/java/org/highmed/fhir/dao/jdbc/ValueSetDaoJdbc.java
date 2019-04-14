package org.highmed.fhir.dao.jdbc;

import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.ValueSetDao;
import org.highmed.fhir.search.parameters.ValueSetIdentifier;
import org.highmed.fhir.search.parameters.ValueSetUrl;
import org.highmed.fhir.search.parameters.ValueSetVersion;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetDaoJdbc extends AbstractDomainResourceDaoJdbc<ValueSet> implements ValueSetDao
{
	private final ReadByUrlJdbc<ValueSet> readByUrl;

	public ValueSetDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, ValueSet.class, "value_sets", "value_set", "value_set_id",
				ValueSetIdentifier::new, ValueSetUrl::new, ValueSetVersion::new);

		readByUrl = new ReadByUrlJdbc<>(this::getDataSource, this::getResource, getResourceTable(), getResourceColumn(),
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
