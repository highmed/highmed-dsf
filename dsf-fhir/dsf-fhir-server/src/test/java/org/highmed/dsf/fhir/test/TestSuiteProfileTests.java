package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.profiles.OrganizationProfileTest;
import org.highmed.dsf.fhir.profiles.TaskProfileTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ OrganizationProfileTest.class, TaskProfileTest.class })
public class TestSuiteProfileTests
{
}
