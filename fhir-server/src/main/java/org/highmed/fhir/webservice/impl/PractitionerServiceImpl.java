package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.PractitionerDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ReferenceExtractor;
import org.highmed.fhir.service.ReferenceResolver;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.PractitionerService;
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerServiceImpl extends AbstractServiceImpl<PractitionerDao, Practitioner>
		implements PractitionerService
{
	public PractitionerServiceImpl(String resourceTypeName, String serverBase, String path, int defaultPageCount,
			PractitionerDao dao, ResourceValidator validator, EventManager eventManager,
			ExceptionHandler exceptionHandler, EventGenerator eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter, ReferenceExtractor referenceExtractor,
			ReferenceResolver referenceResolver)
	{
		super(Practitioner.class, resourceTypeName, serverBase, path, defaultPageCount, dao, validator, eventManager,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter, referenceExtractor,
				referenceResolver);
	}
}
