package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.EndpointDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.EndpointService;
import org.hl7.fhir.r4.model.Endpoint;

public class EndpointServiceImpl extends AbstractServiceImpl<EndpointDao, Endpoint> implements EndpointService
{
	public EndpointServiceImpl(String resourceTypeName, String serverBase, String path, int defaultPageCount,
			EndpointDao dao, ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter)
	{
		super(Endpoint.class, resourceTypeName, serverBase, path, defaultPageCount, dao, validator, eventManager,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter);
	}
}
