package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientDao extends AbstractDomainResourceDao<Patient>
{
	public PatientDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Patient.class, "patients", "patient", "patient_id");
	}

	@Override
	protected Patient copy(Patient resource)
	{
		return resource.copy();
	}

	public PartialResult<Patient> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
