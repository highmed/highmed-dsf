package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.integration.BinaryIntegrationTest;
import org.highmed.dsf.fhir.integration.OrganizationIntegrationTest;
import org.highmed.dsf.fhir.integration.TaskIntegrationTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BinaryIntegrationTest.class, OrganizationIntegrationTest.class, TaskIntegrationTest.class })
public class TestSuiteIntegrationTests
{
	@ClassRule
	public static final FhirEmbeddedPostgresWithLiquibase template = new FhirEmbeddedPostgresWithLiquibase(
			TestSuiteDbTests.template);

	@ClassRule
	public static final X509Certificates certificates = new X509Certificates();
}
