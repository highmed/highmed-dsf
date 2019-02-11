package org.highmed.fhir.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;

public class PatientDaoTest extends AbstractDaoTest<Patient, PatientDao>
{
	private final Date birthday = new GregorianCalendar(1980, 0, 2).getTime();
	private final AdministrativeGender gender = AdministrativeGender.FEMALE;

	@Override
	protected PatientDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new PatientDao(dataSource, fhirContext);
	}

	@Override
	protected IdType createIdWithoutVersion()
	{
		return new IdType("Patient", UUID.randomUUID().toString(), null);
	}

	@Override
	protected IdType createIdWithVersion()
	{
		return new IdType("Patient", UUID.randomUUID().toString(), "1");
	}

	@Override
	protected Patient createResource()
	{
		Patient patient = new Patient();
		patient.setBirthDate(birthday);
		return patient;
	}

	@Override
	protected void checkCreated(Patient resource)
	{
		assertEquals(birthday, resource.getBirthDate());
	}

	@Override
	protected Patient updateResource(Patient resource)
	{
		resource.setGender(gender);
		return resource;
	}

	@Override
	protected void checkUpdates(Patient resource)
	{
		assertEquals(gender, resource.getGender());
	}
}
