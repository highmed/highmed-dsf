package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.OrganizationDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.OrganizationService;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationServiceImpl extends AbstractServiceImpl<OrganizationDao, Organization>
		implements OrganizationService
{
	public OrganizationServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount,
			OrganizationDao dao, ResourceValidator validator, EventManager eventManager,
			ExceptionHandler exceptionHandler, EventGenerator<Organization> eventGenerator,
			ResponseGenerator responseGenerator, ParameterConverter parameterConverter)
	{
		super(resourceTypeName, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter);
	}
}
