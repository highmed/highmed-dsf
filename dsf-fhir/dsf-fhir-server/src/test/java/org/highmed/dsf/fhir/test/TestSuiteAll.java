package org.highmed.dsf.fhir.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestSuiteUnitTests.class, TestSuiteDbTests.class })
public class TestSuiteAll
{
}
