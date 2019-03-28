package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.PatientIdentifier;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientDao extends AbstractDomainResourceDao<Patient>
{
	public PatientDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Patient.class, "patients", "patient", "patient_id", PatientIdentifier::new);
	}

	@Override
	protected Patient copy(Patient resource)
	{
		return resource.copy();
	}
}
