package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class BundleHtmlFhirAdapter extends HtmlFhirAdapter<Bundle>
{
	public BundleHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Bundle.class);
	}
}
