package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.search.parameters.PractitionerActive;
import org.highmed.dsf.fhir.search.parameters.PractitionerIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.PractitionerUserFilter;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerDaoJdbc extends AbstractResourceDaoJdbc<Practitioner> implements PractitionerDao
{
	public PractitionerDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, Practitioner.class, "practitioners", "practitioner",
				"practitioner_id", PractitionerUserFilter::new,
				with(PractitionerActive::new, PractitionerIdentifier::new), with());
	}

	@Override
	protected Practitioner copy(Practitioner resource)
	{
		return resource.copy();
	}
}
