package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.PractitionerRoleDaoJdbc;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

public class PractitionerRoleDaoTest extends AbstractResourceDaoTest<PractitionerRole, PractitionerRoleDao>
{
	private final Date periodStart = new GregorianCalendar(2019, 0, 1).getTime();
	private final Date periodEnd = new GregorianCalendar(2021, 11, 31).getTime();
	private final boolean active = true;

	public PractitionerRoleDaoTest()
	{
		super(PractitionerRole.class);
	}

	@Override
	protected PractitionerRoleDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new PractitionerRoleDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected PractitionerRole createResource()
	{
		PractitionerRole practitionerRole = new PractitionerRole();
		practitionerRole.setActive(true);
		return practitionerRole;
	}

	@Override
	protected void checkCreated(PractitionerRole resource)
	{
		assertEquals(active, resource.getActive());
	}

	@Override
	protected PractitionerRole updateResource(PractitionerRole resource)
	{
		resource.setPeriod(new Period().setStart(periodStart).setEnd(periodEnd));
		return resource;
	}

	@Override
	protected void checkUpdates(PractitionerRole resource)
	{
		assertEquals(periodStart, resource.getPeriod().getStart());
		assertEquals(periodEnd, resource.getPeriod().getEnd());
	}
}
