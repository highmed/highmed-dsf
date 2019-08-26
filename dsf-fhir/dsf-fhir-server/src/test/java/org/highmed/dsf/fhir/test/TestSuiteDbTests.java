package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.dao.*;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BinaryDaoTest.class, BundleDaoTest.class, CodeSystemDaoTest.class, EndpointDaoTest.class, GroupDaoTest.class,
		HealthcareServiceDaoTest.class, LocationDaoTest.class, OrganizationDaoTest.class, PatientDaoTest.class,
		PractitionerDaoTest.class, PractitionerRoleDaoTest.class, ProvenanceDaoTest.class, ResearchStudyDaoTest.class,
		StructureDefinitionDaoTest.class, StructureDefinitionSnapshotDaoTest.class, SubscriptionDaoTest.class,
		TaskDaoTest.class, ValueSetDaoTest.class, TestSuiteIntegrationTests.class })
public class TestSuiteDbTests
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase();
}
