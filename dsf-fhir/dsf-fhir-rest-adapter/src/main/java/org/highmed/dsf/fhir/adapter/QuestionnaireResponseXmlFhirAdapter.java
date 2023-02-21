package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.QuestionnaireResponse;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QuestionnaireResponseXmlFhirAdapter extends XmlFhirAdapter<QuestionnaireResponse>
{
	public QuestionnaireResponseXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, QuestionnaireResponse.class);
	}
}
