package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Measure;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MeasureHtmlFhirAdapter extends HtmlFhirAdapter<Measure>
{
	public MeasureHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Measure.class);
	}
}
