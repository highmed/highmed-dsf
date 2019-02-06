package org.highmed.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PatientXmlAdapter extends AbstractJsonFhirAdapter<Patient>
{
	public PatientXmlAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Patient.class);
	}
}
