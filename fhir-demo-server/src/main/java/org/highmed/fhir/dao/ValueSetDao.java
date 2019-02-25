package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

public class ValueSetDao extends AbstractDomainResourceDao<ValueSet>
{
	public ValueSetDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, ValueSet.class, "value_sets", "value_set", "value_set_id");
	}

	@Override
	protected ValueSet copy(ValueSet resource)
	{
		return resource.copy();
	}
}
