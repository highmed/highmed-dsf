package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.SubscriptionDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.SubscriptionService;
import org.hl7.fhir.r4.model.Subscription;

public class SubscriptionServiceImpl extends AbstractServiceImpl<SubscriptionDao, Subscription>
		implements SubscriptionService
{
	public SubscriptionServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount,
			SubscriptionDao dao, ResourceValidator validator, EventManager eventManager,
			ExceptionHandler exceptionHandler, EventGenerator<Subscription> eventGenerator,
			ResponseGenerator responseGenerator, ParameterConverter parameterConverter)
	{
		super(resourceTypeName, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter);
	}
}
