package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.PractitionerService;
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerServiceSecure extends AbstractServiceSecure<Practitioner, PractitionerService>
		implements PractitionerService
{
	public PractitionerServiceSecure(PractitionerService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
