package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PatientXmlFhirAdapter extends XmlFhirAdapter<Patient>
{
	public PatientXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Patient.class);
	}
}
