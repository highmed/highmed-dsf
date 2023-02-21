package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResearchStudyJsonFhirAdapter extends JsonFhirAdapter<ResearchStudy>
{
	public ResearchStudyJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, ResearchStudy.class);
	}
}
