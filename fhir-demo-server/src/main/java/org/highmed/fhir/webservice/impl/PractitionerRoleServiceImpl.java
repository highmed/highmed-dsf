package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.PractitionerRoleDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleServiceImpl extends AbstractServiceImpl<PractitionerRoleDao, PractitionerRole>
		implements PractitionerRoleService
{
	public PractitionerRoleServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount,
			PractitionerRoleDao dao, ResourceValidator validator, EventManager eventManager,
			ExceptionHandler exceptionHandler, EventGenerator<PractitionerRole> eventGenerator,
			ResponseGenerator responseGenerator, ParameterConverter parameterConverter)
	{
		super(resourceTypeName, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter);
	}
}
