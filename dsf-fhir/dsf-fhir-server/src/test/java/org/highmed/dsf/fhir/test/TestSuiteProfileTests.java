package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.profiles.EndpointProfileTest;
import org.highmed.dsf.fhir.profiles.GroupProfileTest;
import org.highmed.dsf.fhir.profiles.OrganizationProfileTest;
import org.highmed.dsf.fhir.profiles.ResearchStudyProfileTest;
import org.highmed.dsf.fhir.profiles.TaskProfileTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EndpointProfileTest.class, GroupProfileTest.class, OrganizationProfileTest.class,
		ResearchStudyProfileTest.class, TaskProfileTest.class })
public class TestSuiteProfileTests
{
}
