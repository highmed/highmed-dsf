package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.search.parameters.PractitionerActive;
import org.highmed.dsf.fhir.search.parameters.PractitionerIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.PractitionerUserFilter;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerDaoJdbc extends AbstractResourceDaoJdbc<Practitioner> implements PractitionerDao
{
	public PractitionerDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext, OrganizationType organizationType)
	{
		super(dataSource, fhirContext, Practitioner.class, "practitioners", "practitioner", "practitioner_id",
				organizationType, PractitionerUserFilter::new,
				with(PractitionerIdentifier::new, PractitionerActive::new), with());
	}

	@Override
	protected Practitioner copy(Practitioner resource)
	{
		return resource.copy();
	}
}
