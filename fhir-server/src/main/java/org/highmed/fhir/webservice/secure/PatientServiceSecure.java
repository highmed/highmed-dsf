package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.PatientService;
import org.hl7.fhir.r4.model.Patient;

public class PatientServiceSecure extends AbstractServiceSecure<Patient, PatientService> implements PatientService
{
	public PatientServiceSecure(PatientService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
