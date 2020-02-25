package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.PractitionerDaoJdbc;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerDaoTest extends AbstractResourceDaoTest<Practitioner, PractitionerDao>
{
	private final Date birthday = new GregorianCalendar(1980, 0, 2).getTime();
	private final AdministrativeGender gender = AdministrativeGender.FEMALE;

	public PractitionerDaoTest()
	{
		super(Practitioner.class);
	}

	@Override
	protected PractitionerDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new PractitionerDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected Practitioner createResource()
	{
		Practitioner practitioner = new Practitioner();
		practitioner.setBirthDate(birthday);
		return practitioner;
	}

	@Override
	protected void checkCreated(Practitioner resource)
	{
		assertEquals(birthday, resource.getBirthDate());
	}

	@Override
	protected Practitioner updateResource(Practitioner resource)
	{
		resource.setGender(gender);
		return resource;
	}

	@Override
	protected void checkUpdates(Practitioner resource)
	{
		assertEquals(gender, resource.getGender());
	}
}
