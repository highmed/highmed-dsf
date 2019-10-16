package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.dao.command.ResourceReferenceTest;
import org.highmed.dsf.fhir.hapi.BundleTest;
import org.highmed.dsf.fhir.hapi.CodeSystemTest;
import org.highmed.dsf.fhir.hapi.EndpointTest;
import org.highmed.dsf.fhir.hapi.IdTypeTest;
import org.highmed.dsf.fhir.hapi.OrganizationTest;
import org.highmed.dsf.fhir.hapi.ParametersTest;
import org.highmed.dsf.fhir.hapi.ParserTest;
import org.highmed.dsf.fhir.hapi.ReferenceTypTest;
import org.highmed.dsf.fhir.hapi.SerializationTest;
import org.highmed.dsf.fhir.hapi.SnapshotTest;
import org.highmed.dsf.fhir.hapi.StructureDefinitionTreeTest;
import org.highmed.dsf.fhir.hapi.SubscriptionTest;
import org.highmed.dsf.fhir.hapi.ValidationTest;
import org.highmed.dsf.fhir.profiles.ProfileTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ResourceReferenceTest.class, BundleTest.class, CodeSystemTest.class, EndpointTest.class,
		IdTypeTest.class, OrganizationTest.class, ParametersTest.class, ParserTest.class, ReferenceTypTest.class,
		SerializationTest.class, SnapshotTest.class, StructureDefinitionTreeTest.class, SubscriptionTest.class,
		ValidationTest.class, ProfileTests.class })
public class TestSuiteUnitTests
{
}
