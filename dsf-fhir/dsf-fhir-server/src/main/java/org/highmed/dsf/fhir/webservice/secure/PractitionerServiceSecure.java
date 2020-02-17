package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.dao.PractitionerDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.PractitionerService;
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerServiceSecure extends
		AbstractResourceServiceSecure<PractitionerDao, Practitioner, PractitionerService> implements PractitionerService
{
	public PractitionerServiceSecure(PractitionerService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, PractitionerDao practitionerDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Practitioner.class, practitionerDao,
				exceptionHandler, parameterConverter);
	}
}
