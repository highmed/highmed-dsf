package org.highmed.fhir.werbservice;

import javax.ws.rs.Path;

import org.hl7.fhir.r4.model.Patient;

@Path(PatientService.PATH)
public class PatientService extends AbstractService<Patient>
{
	public static final String PATH = "Patient";

	public PatientService()
	{
		super(PATH);
	}
}
