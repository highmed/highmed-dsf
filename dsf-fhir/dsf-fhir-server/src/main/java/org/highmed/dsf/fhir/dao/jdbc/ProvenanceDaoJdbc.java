package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.ProvenanceDao;
import org.highmed.dsf.fhir.search.parameters.user.PatientUserFilter;
import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;

public class ProvenanceDaoJdbc extends AbstractResourceDaoJdbc<Provenance> implements ProvenanceDao
{
	public ProvenanceDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext, OrganizationType organizationType)
	{
		super(dataSource, fhirContext, Provenance.class, "provenances", "provenance", "provenance_id", organizationType,
				PatientUserFilter::new, with(), with());
	}

	@Override
	protected Provenance copy(Provenance resource)
	{
		return resource.copy();
	}
}
