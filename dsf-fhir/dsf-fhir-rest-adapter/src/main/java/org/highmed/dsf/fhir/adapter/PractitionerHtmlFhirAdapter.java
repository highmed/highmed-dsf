package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PractitionerHtmlFhirAdapter extends HtmlFhirAdapter<Practitioner>
{
	public PractitionerHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Practitioner.class);
	}
}
