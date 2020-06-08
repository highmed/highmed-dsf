package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.client.ClientProviderTest;
import org.highmed.dsf.fhir.dao.command.ResourceReferenceTest;
import org.highmed.dsf.fhir.service.SnapshotDependencyAnalyzerTest;
import org.highmed.dsf.fhir.service.SnapshotInfoJsonTest;
import org.highmed.dsf.fhir.service.ValueSetExpanderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestSuiteHapiLearningAndBugValidationTests.class, TestSuiteProfileTests.class, ClientProviderTest.class,
		ResourceReferenceTest.class, SnapshotDependencyAnalyzerTest.class, SnapshotInfoJsonTest.class,
		ValueSetExpanderTest.class })
public class TestSuiteUnitTests
{
}
