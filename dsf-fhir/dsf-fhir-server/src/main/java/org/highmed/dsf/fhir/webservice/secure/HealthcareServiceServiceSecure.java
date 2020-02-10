package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.HealthcareServiceService;
import org.hl7.fhir.r4.model.HealthcareService;

public class HealthcareServiceServiceSecure extends AbstractResourceServiceSecure<HealthcareService, HealthcareServiceService>
		implements HealthcareServiceService
{
	public HealthcareServiceServiceSecure(HealthcareServiceService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
