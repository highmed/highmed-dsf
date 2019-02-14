package org.highmed.fhir.test;

import org.highmed.fhir.hapi.SerializationTest;
import org.highmed.fhir.hapi.SnapshotTest;
import org.highmed.fhir.hapi.ValidationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SerializationTest.class, SnapshotTest.class, ValidationTest.class })
public class TestSuiteUnitTests
{
}
