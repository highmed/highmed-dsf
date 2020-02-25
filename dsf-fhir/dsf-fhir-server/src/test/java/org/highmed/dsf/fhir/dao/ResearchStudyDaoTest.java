package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.jdbc.ResearchStudyDaoJdbc;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.ResearchStudy;

import ca.uhn.fhir.context.FhirContext;

public class ResearchStudyDaoTest extends AbstractResourceDaoTest<ResearchStudy, ResearchStudyDao>
{
	private static final String title = "Demo Research Study";
	private final Date periodStart = new GregorianCalendar(2019, 0, 1).getTime();
	private final Date periodEnd = new GregorianCalendar(2021, 11, 31).getTime();

	public ResearchStudyDaoTest()
	{
		super(ResearchStudy.class);
	}

	@Override
	protected ResearchStudyDao createDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		return new ResearchStudyDaoJdbc(dataSource, fhirContext);
	}

	@Override
	protected ResearchStudy createResource()
	{
		ResearchStudy researchStudy = new ResearchStudy();
		researchStudy.setTitle(title);
		return researchStudy;
	}

	@Override
	protected void checkCreated(ResearchStudy resource)
	{
		assertEquals(title, resource.getTitle());
	}

	@Override
	protected ResearchStudy updateResource(ResearchStudy resource)
	{
		resource.setPeriod(new Period().setStart(periodStart).setEnd(periodEnd));
		return resource;
	}

	@Override
	protected void checkUpdates(ResearchStudy resource)
	{
		assertEquals(periodStart, resource.getPeriod().getStart());
		assertEquals(periodEnd, resource.getPeriod().getEnd());
	}
}
