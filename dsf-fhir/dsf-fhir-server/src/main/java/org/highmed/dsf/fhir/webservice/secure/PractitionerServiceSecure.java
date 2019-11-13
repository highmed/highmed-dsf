package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.PractitionerService;
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerServiceSecure extends AbstractServiceSecure<Practitioner, PractitionerService>
		implements PractitionerService
{
	public PractitionerServiceSecure(PractitionerService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
