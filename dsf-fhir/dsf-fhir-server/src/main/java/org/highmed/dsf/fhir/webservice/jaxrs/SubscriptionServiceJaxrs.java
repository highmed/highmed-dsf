package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.SubscriptionService;
import org.hl7.fhir.r4.model.Subscription;

@Path(SubscriptionServiceJaxrs.PATH)
public class SubscriptionServiceJaxrs extends AbstractResourceServiceJaxrs<Subscription, SubscriptionService>
		implements SubscriptionService
{
	public static final String PATH = "Subscription";

	public SubscriptionServiceJaxrs(SubscriptionService delegate)
	{
		super(delegate);
	}
}
