package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.SubscriptionDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.SubscriptionService;
import org.hl7.fhir.r4.model.Subscription;

public class SubscriptionServiceImpl extends AbstractServiceImpl<SubscriptionDao, Subscription>
		implements SubscriptionService
{
	public SubscriptionServiceImpl(String serverBase, int defaultPageCount, SubscriptionDao subscriptionDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<Subscription> serviceHelper)
	{
		super(serverBase, defaultPageCount, subscriptionDao, validator, eventManager, serviceHelper);
	}
}
