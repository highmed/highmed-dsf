package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.PatientDao;
import org.hl7.fhir.r4.model.Patient;

@Path(PatientService.RESOURCE_TYPE_NAME)
public class PatientService extends AbstractService<PatientDao, Patient>
{
	public static final String RESOURCE_TYPE_NAME = "Patient";

	public PatientService(String serverBase, int defaultPageCount, PatientDao patientDao)
	{
		super(serverBase, defaultPageCount, RESOURCE_TYPE_NAME, patientDao);
	}
}
