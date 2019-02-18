package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.webservice.specification.HealthcareServiceService;
import org.hl7.fhir.r4.model.HealthcareService;

public class HealthcareServiceServiceSecure extends AbstractServiceSecure<HealthcareService, HealthcareServiceService>
		implements HealthcareServiceService
{
	public HealthcareServiceServiceSecure(HealthcareServiceService delegate)
	{
		super(delegate);
	}
}
