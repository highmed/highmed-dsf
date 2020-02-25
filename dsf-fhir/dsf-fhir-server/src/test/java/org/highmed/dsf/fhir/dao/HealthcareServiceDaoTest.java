package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.HealthcareServiceDaoJdbc;
import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.context.FhirContext;

public class HealthcareServiceDaoTest extends AbstractResourceDaoTest<HealthcareService, HealthcareServiceDao>
{
	private static final String name = "Demo Healthcare Service";
	private static final boolean appointmentRequired = true;

	public HealthcareServiceDaoTest()
	{
		super(HealthcareService.class);
	}

	@Override
	protected HealthcareServiceDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new HealthcareServiceDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected HealthcareService createResource()
	{
		HealthcareService healthcareService = new HealthcareService();
		healthcareService.setName(name);
		return healthcareService;
	}

	@Override
	protected void checkCreated(HealthcareService resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected HealthcareService updateResource(HealthcareService resource)
	{
		resource.setAppointmentRequired(true);
		return resource;
	}

	@Override
	protected void checkUpdates(HealthcareService resource)
	{
		assertEquals(appointmentRequired, resource.getAppointmentRequired());
	}
}
