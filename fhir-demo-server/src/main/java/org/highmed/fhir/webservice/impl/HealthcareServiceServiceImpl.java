package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.HealthcareServiceDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.HealthcareServiceService;
import org.hl7.fhir.r4.model.HealthcareService;

public class HealthcareServiceServiceImpl extends AbstractServiceImpl<HealthcareServiceDao, HealthcareService>
		implements HealthcareServiceService
{
	public HealthcareServiceServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount,
			HealthcareServiceDao dao, ResourceValidator validator, EventManager eventManager,
			ExceptionHandler exceptionHandler, EventGenerator<HealthcareService> eventGenerator,
			ResponseGenerator responseGenerator, ParameterConverter parameterConverter)
	{
		super(resourceTypeName, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter);
	}
}
