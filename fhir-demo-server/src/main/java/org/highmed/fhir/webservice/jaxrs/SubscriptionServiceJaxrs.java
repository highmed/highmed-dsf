package org.highmed.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.fhir.webservice.specification.SubscriptionService;
import org.hl7.fhir.r4.model.Subscription;

@Path(SubscriptionServiceJaxrs.PATH)
public class SubscriptionServiceJaxrs extends AbstractServiceJaxrs<Subscription, SubscriptionService>
		implements SubscriptionService
{
	public static final String PATH = "Subscription";

	public SubscriptionServiceJaxrs(SubscriptionService delegate)
	{
		super(delegate);
	}

	@Override
	public String getPath()
	{
		return PATH;
	}
}
