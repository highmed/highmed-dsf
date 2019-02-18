package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.webservice.specification.SubscriptionService;
import org.hl7.fhir.r4.model.Subscription;

public class SubscriptionServiceSecure extends AbstractServiceSecure<Subscription, SubscriptionService>
		implements SubscriptionService
{
	public SubscriptionServiceSecure(SubscriptionService delegate)
	{
		super(delegate);
	}
}
