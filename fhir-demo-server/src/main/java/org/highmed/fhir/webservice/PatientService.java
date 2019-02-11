package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.BasicCrudDao;
import org.hl7.fhir.r4.model.Patient;

@Path(PatientService.RESOURCE_TYPE_NAME)
public class PatientService extends AbstractService<Patient>
{
	public static final String RESOURCE_TYPE_NAME = "Patient";

	public PatientService(String serverBase, BasicCrudDao<Patient> crudDao)
	{
		super(serverBase, RESOURCE_TYPE_NAME, crudDao);
	}
}
