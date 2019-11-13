package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.search.parameters.PractitionerActive;
import org.highmed.dsf.fhir.search.parameters.PractitionerIdentifier;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerDaoJdbc extends AbstractResourceDaoJdbc<Practitioner> implements PractitionerDao
{
	public PractitionerDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
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
