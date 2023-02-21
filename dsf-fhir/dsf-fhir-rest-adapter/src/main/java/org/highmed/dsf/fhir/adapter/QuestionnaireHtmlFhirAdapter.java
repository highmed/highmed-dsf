package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Questionnaire;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuestionnaireHtmlFhirAdapter extends HtmlFhirAdapter<Questionnaire>
{
	public QuestionnaireHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Questionnaire.class);
	}
}
