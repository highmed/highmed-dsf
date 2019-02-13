package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.SubscriptionDao;
import org.hl7.fhir.r4.model.Subscription;

@Path(SubscriptionService.RESOURCE_TYPE)
public class SubscriptionService extends AbstractService<SubscriptionDao, Subscription>
{
	public static final String RESOURCE_TYPE = "Subscription";

	public SubscriptionService(String serverBase, int defaultPageCount, SubscriptionDao subscriptionDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE, subscriptionDao);
	}
}
