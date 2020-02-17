package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.dao.HealthcareServiceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.HealthcareServiceService;
import org.hl7.fhir.r4.model.HealthcareService;

public class HealthcareServiceServiceSecure
		extends AbstractResourceServiceSecure<HealthcareServiceDao, HealthcareService, HealthcareServiceService>
		implements HealthcareServiceService
{
	public HealthcareServiceServiceSecure(HealthcareServiceService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver,
			HealthcareServiceDao healthcareServiceDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, HealthcareService.class, healthcareServiceDao,
				exceptionHandler, parameterConverter);
	}
}
