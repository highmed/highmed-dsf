package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.PatientDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.PatientService;
import org.hl7.fhir.r4.model.Patient;

public class PatientServiceImpl extends AbstractServiceImpl<PatientDao, Patient> implements PatientService
{
	public PatientServiceImpl(String serverBase, int defaultPageCount, PatientDao patientDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<Patient> serviceHelper)
	{
		super(serverBase, defaultPageCount, patientDao, validator, eventManager, serviceHelper);
	}
}
