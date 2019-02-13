package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerDao extends AbstractDomainResourceDao<Practitioner>
{
	public PractitionerDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Practitioner.class, "practitioners", "practitioner", "practitioner_id");
	}

	@Override
	protected Practitioner copy(Practitioner resource)
	{
		return resource.copy();
	}

	public PartialResult<Practitioner> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
