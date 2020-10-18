package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.highmed.dsf.fhir.OrganizationType;
import org.highmed.dsf.fhir.dao.jdbc.ResearchStudyDaoJdbc;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResearchStudyDaoTest extends AbstractResourceDaoTest<ResearchStudy, ResearchStudyDao>
{
	private static final Logger logger = LoggerFactory.getLogger(ResearchStudyDaoTest.class);

	private static final String title = "Demo Research Study";
	private final Date periodStart = new GregorianCalendar(2019, 0, 1).getTime();
	private final Date periodEnd = new GregorianCalendar(2021, 11, 31).getTime();

	public ResearchStudyDaoTest()
	{
		super(ResearchStudy.class, ResearchStudyDaoJdbc::new);
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

	@Test
	public void testReadByPrincipalInvestigatorIdAndOrganizationTypeAndOrganizationIdWithTransaction() throws Exception
	{
		String piReference = "Practitioner/" + UUID.randomUUID().toString();
		String orgReference = "Organization/" + UUID.randomUUID().toString();

		ResearchStudy r = new ResearchStudy();
		r.setPrincipalInvestigator(new Reference(piReference));
		r.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-ttp")
				.setValue(new Reference(orgReference));
		r.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic")
				.setValue(new Reference("Organization/" + UUID.randomUUID().toString()));
		r.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic")
				.setValue(new Reference("Organization/" + UUID.randomUUID().toString()));

		logger.debug(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(r));

		dao.create(r);

		try (Connection connection = dao.newReadWriteTransaction())
		{
			boolean exists = dao.existsByPrincipalInvestigatorIdAndOrganizationTypeAndOrganizationIdWithTransaction(
					connection, new IdType(piReference), OrganizationType.TTP, new IdType(orgReference));
			assertTrue(exists);
		}
	}

	@Test
	public void testReadByEnrollmentIdAndOrganizationTypeAndOrganizationIdWithTransaction() throws Exception
	{
		String enrollmentReference1 = "Group/" + UUID.randomUUID().toString();
		String enrollmentReference2 = "Group/" + UUID.randomUUID().toString();
		String orgReference = "Organization/" + UUID.randomUUID().toString();

		ResearchStudy r = new ResearchStudy();
		r.addEnrollment(new Reference(enrollmentReference1));
		r.addEnrollment(new Reference(enrollmentReference2));
		r.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-ttp")
				.setValue(new Reference("Organization/" + UUID.randomUUID().toString()));
		r.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic")
				.setValue(new Reference(orgReference));
		r.addExtension().setUrl("http://highmed.org/fhir/StructureDefinition/participating-medic")
				.setValue(new Reference("Organization/" + UUID.randomUUID().toString()));

		logger.debug(fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(r));

		dao.create(r);

		try (Connection connection = dao.newReadWriteTransaction())
		{
			boolean exists = dao.existsByEnrollmentIdAndOrganizationTypeAndOrganizationIdWithTransaction(connection,
					new IdType(enrollmentReference1), OrganizationType.MeDIC, new IdType(orgReference));
			assertTrue(exists);
		}
	}
}
