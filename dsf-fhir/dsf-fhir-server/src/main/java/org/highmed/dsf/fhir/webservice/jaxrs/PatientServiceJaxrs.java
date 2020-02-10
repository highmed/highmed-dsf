package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.PatientService;
import org.hl7.fhir.r4.model.Patient;

@Path(PatientServiceJaxrs.PATH)
public class PatientServiceJaxrs extends AbstractResourceServiceJaxrs<Patient, PatientService> implements PatientService
{
	public static final String PATH = "Patient";

	public PatientServiceJaxrs(PatientService delegate)
	{
		super(delegate);
	}
}
