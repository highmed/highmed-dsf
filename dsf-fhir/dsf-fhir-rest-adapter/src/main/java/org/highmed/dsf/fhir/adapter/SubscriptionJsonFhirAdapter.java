package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SubscriptionJsonFhirAdapter extends JsonFhirAdapter<Subscription>
{
	public SubscriptionJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Subscription.class);
	}
}
