package org.highmed.fhir.adapter;

import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;

public class SubscriptionJsonFhirAdapter extends JsonFhirAdapter<Subscription>
{
	public SubscriptionJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Subscription.class);
	}
}
