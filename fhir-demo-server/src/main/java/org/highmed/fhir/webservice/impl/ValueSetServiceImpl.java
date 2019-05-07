package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.ValueSetDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ReferenceExtractor;
import org.highmed.fhir.service.ReferenceResolver;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.ValueSetService;
import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetServiceImpl extends AbstractServiceImpl<ValueSetDao, ValueSet> implements ValueSetService
{
	public ValueSetServiceImpl(String resourceTypeName, String serverBase, String path, int defaultPageCount,
			ValueSetDao dao, ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver)
	{
		super(ValueSet.class, resourceTypeName, serverBase, path, defaultPageCount, dao, validator, eventManager,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter, referenceExtractor,
				referenceResolver);
	}
}
