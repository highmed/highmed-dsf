package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Questionnaire;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuestionnaireJsonFhirAdapter extends JsonFhirAdapter<Questionnaire>
{
	public QuestionnaireJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Questionnaire.class);
	}
}
