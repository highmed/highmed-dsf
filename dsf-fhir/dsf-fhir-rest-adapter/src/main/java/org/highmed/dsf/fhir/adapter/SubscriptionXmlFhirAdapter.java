package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Subscription;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class SubscriptionXmlFhirAdapter extends XmlFhirAdapter<Subscription>
{
	public SubscriptionXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Subscription.class);
	}
}
