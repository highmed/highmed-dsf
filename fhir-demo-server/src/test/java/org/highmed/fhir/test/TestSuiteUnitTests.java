package org.highmed.fhir.test;

import org.highmed.fhir.dao.command.ResourceReferenceTest;
import org.highmed.fhir.hapi.CodeSystemTest;
import org.highmed.fhir.hapi.EndpointTest;
import org.highmed.fhir.hapi.HiGHmedTaskValidationTest;
import org.highmed.fhir.hapi.IdTypeTest;
import org.highmed.fhir.hapi.OrganizationTest;
import org.highmed.fhir.hapi.ParametersTest;
import org.highmed.fhir.hapi.ReferenceTypTest;
import org.highmed.fhir.hapi.SerializationTest;
import org.highmed.fhir.hapi.SnapshotTest;
import org.highmed.fhir.hapi.StructureDefinitionTreeTest;
import org.highmed.fhir.hapi.SubscriptionTest;
import org.highmed.fhir.hapi.ValidationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ResourceReferenceTest.class, CodeSystemTest.class, EndpointTest.class, HiGHmedTaskValidationTest.class, IdTypeTest.class,
		OrganizationTest.class, ParametersTest.class, SerializationTest.class, ReferenceTypTest.class,
		SerializationTest.class, SnapshotTest.class, StructureDefinitionTreeTest.class, SubscriptionTest.class,
		ValidationTest.class })
public class TestSuiteUnitTests
{
}
