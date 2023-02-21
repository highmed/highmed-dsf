package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BundleHtmlFhirAdapter extends HtmlFhirAdapter<Bundle>
{
	public BundleHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Bundle.class);
	}
}
