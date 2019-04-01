package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.PractitionerActive;
import org.highmed.fhir.search.parameters.PractitionerIdentifier;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerDao extends AbstractDomainResourceDao<Practitioner>
{
	public PractitionerDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Practitioner.class, "practitioners", "practitioner", "practitioner_id",
				PractitionerIdentifier::new, PractitionerActive::new);
	}

	@Override
	protected Practitioner copy(Practitioner resource)
	{
		return resource.copy();
	}
}
