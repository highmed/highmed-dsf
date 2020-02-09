package org.highmed.dsf.fhir.webservice.impl;

import org.highmed.dsf.fhir.dao.PatientDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.PatientService;
import org.hl7.fhir.r4.model.Patient;

public class PatientServiceImpl extends AbstractResourceServiceImpl<PatientDao, Patient> implements PatientService
{
	public PatientServiceImpl(String path, String serverBase, int defaultPageCount, PatientDao dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver)
	{
		super(path, Patient.class, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter, referenceExtractor, referenceResolver);
	}
}
