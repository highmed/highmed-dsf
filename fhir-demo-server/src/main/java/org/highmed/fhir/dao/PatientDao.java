package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientDao extends AbstractDao<Patient>
{
	public PatientDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Patient.class, "patients", "patient", "patient_id");
	}

	@Override
	protected Patient copy(Patient resource)
	{
		return resource.copy();
	}
}
