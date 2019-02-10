package org.highmed.fhir.adapter;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientJsonFhirAdapter extends JsonFhirAdapter<Patient>
{
	public PatientJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Patient.class);
	}
}
