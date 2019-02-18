package org.highmed.fhir.webservice.specification;

import javax.ws.rs.Path;

import org.hl7.fhir.r4.model.Subscription;

@Path(SubscriptionService.PATH)
public interface SubscriptionService extends BasicService<Subscription>
{
	final String PATH = "Subscription";
}
