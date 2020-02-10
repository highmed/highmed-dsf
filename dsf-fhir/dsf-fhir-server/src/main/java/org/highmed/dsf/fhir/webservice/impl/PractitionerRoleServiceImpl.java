package org.highmed.dsf.fhir.webservice.impl;

import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleServiceImpl extends AbstractResourceServiceImpl<PractitionerRoleDao, PractitionerRole>
		implements PractitionerRoleService
{
	public PractitionerRoleServiceImpl(String path, String serverBase, int defaultPageCount, PractitionerRoleDao dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver)
	{
		super(path, PractitionerRole.class, serverBase, defaultPageCount, dao, validator, eventManager,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter, referenceExtractor,
				referenceResolver);
	}
}
