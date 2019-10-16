package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class ResearchStudyXmlFhirAdapter extends XmlFhirAdapter<ResearchStudy>
{
	public ResearchStudyXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, ResearchStudy.class);
	}
}
