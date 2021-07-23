package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.ProvenanceDao;
import org.highmed.dsf.fhir.search.parameters.user.PatientUserFilter;
import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;

public class ProvenanceDaoJdbc extends AbstractResourceDaoJdbc<Provenance> implements ProvenanceDao
{
	public ProvenanceDaoJdbc(DataSource dataSource, DataSource deletionDataSource, FhirContext fhirContext)
	{
		super(dataSource, deletionDataSource, fhirContext, Provenance.class, "provenances", "provenance",
				"provenance_id", PatientUserFilter::new, with(), with());
	}

	@Override
	protected Provenance copy(Provenance resource)
	{
		return resource.copy();
	}
}
