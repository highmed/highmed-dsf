package org.highmed.fhir.adapter;

import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;

public class SubscriptionXmlFhirAdapter extends XmlFhirAdapter<Subscription>
{
	public SubscriptionXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Subscription.class);
	}
}
