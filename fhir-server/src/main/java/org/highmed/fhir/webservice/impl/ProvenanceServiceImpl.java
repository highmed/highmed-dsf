package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.ProvenanceDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ReferenceExtractor;
import org.highmed.fhir.service.ReferenceResolver;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.ProvenanceService;
import org.hl7.fhir.r4.model.Provenance;

public class ProvenanceServiceImpl extends AbstractServiceImpl<ProvenanceDao, Provenance> implements ProvenanceService
{
	public ProvenanceServiceImpl(String resourceTypeName, String serverBase, String path, int defaultPageCount,
			ProvenanceDao dao, ResourceValidator validator, EventManager eventManager,
			ExceptionHandler exceptionHandler, EventGenerator eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter, ReferenceExtractor referenceExtractor,
			ReferenceResolver referenceResolver)
	{
		super(Provenance.class, resourceTypeName, serverBase, path, defaultPageCount, dao, validator, eventManager,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter, referenceExtractor,
				referenceResolver);
	}
}
