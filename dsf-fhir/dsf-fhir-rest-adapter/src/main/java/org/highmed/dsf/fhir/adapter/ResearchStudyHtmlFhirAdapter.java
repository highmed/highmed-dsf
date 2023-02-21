package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResearchStudyHtmlFhirAdapter extends HtmlFhirAdapter<ResearchStudy>
{
	public ResearchStudyHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, ResearchStudy.class);
	}
}
