package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.CodeSystemDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.CodeSystemService;
import org.hl7.fhir.r4.model.CodeSystem;

public class CodeSystemServiceImpl extends AbstractServiceImpl<CodeSystemDao, CodeSystem> implements CodeSystemService
{
	public CodeSystemServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount, CodeSystemDao dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator<CodeSystem> eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter)
	{
		super(resourceTypeName, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter);
	}
}
