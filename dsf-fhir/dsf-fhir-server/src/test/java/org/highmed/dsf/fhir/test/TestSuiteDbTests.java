package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.dao.BinaryDaoTest;
import org.highmed.dsf.fhir.dao.BundleDaoTest;
import org.highmed.dsf.fhir.dao.CodeSystemDaoTest;
import org.highmed.dsf.fhir.dao.EndpointDaoTest;
import org.highmed.dsf.fhir.dao.GroupDaoTest;
import org.highmed.dsf.fhir.dao.HealthcareServiceDaoTest;
import org.highmed.dsf.fhir.dao.LocationDaoTest;
import org.highmed.dsf.fhir.dao.NamingSystemDaoTest;
import org.highmed.dsf.fhir.dao.OrganizationDaoTest;
import org.highmed.dsf.fhir.dao.PatientDaoTest;
import org.highmed.dsf.fhir.dao.PractitionerDaoTest;
import org.highmed.dsf.fhir.dao.PractitionerRoleDaoTest;
import org.highmed.dsf.fhir.dao.ProvenanceDaoTest;
import org.highmed.dsf.fhir.dao.ResearchStudyDaoTest;
import org.highmed.dsf.fhir.dao.StructureDefinitionDaoTest;
import org.highmed.dsf.fhir.dao.StructureDefinitionSnapshotDaoTest;
import org.highmed.dsf.fhir.dao.SubscriptionDaoTest;
import org.highmed.dsf.fhir.dao.TaskDaoTest;
import org.highmed.dsf.fhir.dao.ValueSetDaoTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BinaryDaoTest.class, BundleDaoTest.class, CodeSystemDaoTest.class, EndpointDaoTest.class,
		GroupDaoTest.class, HealthcareServiceDaoTest.class, LocationDaoTest.class, NamingSystemDaoTest.class,
		OrganizationDaoTest.class, PatientDaoTest.class, PractitionerDaoTest.class, PractitionerRoleDaoTest.class,
		ProvenanceDaoTest.class, ResearchStudyDaoTest.class, StructureDefinitionDaoTest.class,
		StructureDefinitionSnapshotDaoTest.class, SubscriptionDaoTest.class, TaskDaoTest.class, ValueSetDaoTest.class,
		TestSuiteIntegrationTests.class })
public class TestSuiteDbTests
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase();
}
