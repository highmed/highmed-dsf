package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;

public class ProvenanceDao extends AbstractDomainResourceDao<Provenance>
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
}
