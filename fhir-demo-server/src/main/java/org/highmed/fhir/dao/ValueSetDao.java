package org.highmed.fhir.dao;

import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.ValueSetUrl;
import org.highmed.fhir.search.parameters.ValueSetVersion;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetDao extends AbstractDomainResourceDao<ValueSet>
{
	private final ReadByUrl<ValueSet> readByUrl;

	public ValueSetDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, ValueSet.class, "value_sets", "value_set", "value_set_id",
				() -> new ValueSetUrl(), () -> new ValueSetVersion());

		readByUrl = new ReadByUrl<ValueSet>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn(), getResourceIdColumn());
	}

	@Override
	protected ValueSet copy(ValueSet resource)
	{
		return resource.copy();
	}

	public Optional<ValueSet> readByUrl(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrl(urlAndVersion);
	}
}
