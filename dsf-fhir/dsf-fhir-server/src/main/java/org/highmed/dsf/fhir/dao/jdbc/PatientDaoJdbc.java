package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.PatientDao;
import org.highmed.dsf.fhir.search.parameters.PatientActive;
import org.highmed.dsf.fhir.search.parameters.PatientIdentifier;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientDaoJdbc extends AbstractResourceDaoJdbc<Patient> implements PatientDao
{
	public PatientDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Patient.class, "patients", "patient", "patient_id", PatientIdentifier::new,
				PatientActive::new);
	}

	@Override
	protected Patient copy(Patient resource)
	{
		return resource.copy();
	}
}
