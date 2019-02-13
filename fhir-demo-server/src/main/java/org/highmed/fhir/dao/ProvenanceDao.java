package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;

public class ProvenanceDao extends AbstractDao<Provenance>
{
	public ProvenanceDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Provenance.class, "provenances", "provenance", "provenance_id");
	}

	@Override
	protected Provenance copy(Provenance resource)
	{
		return resource.copy();
	}

	public PartialResult<Provenance> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
