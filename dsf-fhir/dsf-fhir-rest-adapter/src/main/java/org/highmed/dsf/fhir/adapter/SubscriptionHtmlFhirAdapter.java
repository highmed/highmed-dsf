package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SubscriptionHtmlFhirAdapter extends HtmlFhirAdapter<Subscription>
{
	public SubscriptionHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Subscription.class);
	}
}
