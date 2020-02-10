package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PatientHtmlFhirAdapter extends HtmlFhirAdapter<Patient>
{
	public PatientHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Patient.class);
	}
}
