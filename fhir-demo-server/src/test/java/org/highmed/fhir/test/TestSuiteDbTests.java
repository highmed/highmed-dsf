package org.highmed.fhir.test;

import org.highmed.fhir.dao.HealthcareServiceDaoTest;
import org.highmed.fhir.dao.LocationDaoTest;
import org.highmed.fhir.dao.OrganizationDaoTest;
import org.highmed.fhir.dao.PatientDaoTest;
import org.highmed.fhir.dao.PractitionerDaoTest;
import org.highmed.fhir.dao.PractitionerRoleDaoTest;
import org.highmed.fhir.dao.ProvenanceDaoTest;
import org.highmed.fhir.dao.ResearchStudyDaoTest;
import org.highmed.fhir.dao.StructureDefinitionDaoTest;
import org.highmed.fhir.dao.SubscriptionDaoTest;
import org.highmed.fhir.dao.TaskDaoTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ HealthcareServiceDaoTest.class, LocationDaoTest.class, OrganizationDaoTest.class, PatientDaoTest.class,
		PractitionerDaoTest.class, PractitionerRoleDaoTest.class, ProvenanceDaoTest.class, ResearchStudyDaoTest.class,
		StructureDefinitionDaoTest.class, SubscriptionDaoTest.class, TaskDaoTest.class })
public class TestSuiteDbTests
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase();
}
