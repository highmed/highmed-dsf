package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.PatientService;
import org.hl7.fhir.r4.model.Patient;

public class PatientServiceSecure extends AbstractServiceSecure<Patient, PatientService> implements PatientService
{
	public PatientServiceSecure(PatientService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
