package org.highmed.dsf.fhir.webservice.impl;

import org.highmed.dsf.fhir.dao.ProvenanceDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.ProvenanceService;
import org.hl7.fhir.r4.model.Provenance;

public class ProvenanceServiceImpl extends AbstractResourceServiceImpl<ProvenanceDao, Provenance>
		implements ProvenanceService
{
	public ProvenanceServiceImpl(String path, String serverBase, int defaultPageCount, ProvenanceDao dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver)
	{
		super(path, Provenance.class, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter, referenceExtractor, referenceResolver);
	}
}
