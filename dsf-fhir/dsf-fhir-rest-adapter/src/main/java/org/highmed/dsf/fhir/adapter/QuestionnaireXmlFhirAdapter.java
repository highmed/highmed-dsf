package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Questionnaire;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuestionnaireXmlFhirAdapter extends XmlFhirAdapter<Questionnaire>
{
	public QuestionnaireXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Questionnaire.class);
	}
}
