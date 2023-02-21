package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Parameters;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ParametersHtmlFhirAdapter extends HtmlFhirAdapter<Parameters>
{
	public ParametersHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Parameters.class);
	}
}
