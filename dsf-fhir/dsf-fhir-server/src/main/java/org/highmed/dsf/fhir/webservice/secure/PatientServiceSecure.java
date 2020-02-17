package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.dao.PatientDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.PatientService;
import org.hl7.fhir.r4.model.Patient;

public class PatientServiceSecure extends AbstractResourceServiceSecure<PatientDao, Patient, PatientService>
		implements PatientService
{
	public PatientServiceSecure(PatientService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, PatientDao patientDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Patient.class, patientDao, exceptionHandler,
				parameterConverter);
	}
}
