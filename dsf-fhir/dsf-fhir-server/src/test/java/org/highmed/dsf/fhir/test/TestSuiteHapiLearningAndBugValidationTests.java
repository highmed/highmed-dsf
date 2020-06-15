package org.highmed.dsf.fhir.test;

import org.highmed.dsf.fhir.hapi.ActivityDefinitionWithExtension;
import org.highmed.dsf.fhir.hapi.BinaryTest;
import org.highmed.dsf.fhir.hapi.BundleTest;
import org.highmed.dsf.fhir.hapi.CodeSystemTest;
import org.highmed.dsf.fhir.hapi.EndpointTest;
import org.highmed.dsf.fhir.hapi.IdTypeTest;
import org.highmed.dsf.fhir.hapi.MetaTest;
import org.highmed.dsf.fhir.hapi.OrganizationTest;
import org.highmed.dsf.fhir.hapi.ParametersTest;
import org.highmed.dsf.fhir.hapi.ParserTest;
import org.highmed.dsf.fhir.hapi.ReferenceTest;
import org.highmed.dsf.fhir.hapi.ReferenceTypTest;
import org.highmed.dsf.fhir.hapi.ResearchStudyTest;
import org.highmed.dsf.fhir.hapi.SerializationTest;
import org.highmed.dsf.fhir.hapi.StructureDefinitionTreeTest;
import org.highmed.dsf.fhir.hapi.SubscriptionTest;
import org.highmed.dsf.fhir.hapi.TaskTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ActivityDefinitionWithExtension.class, BinaryTest.class, BundleTest.class, CodeSystemTest.class,
		EndpointTest.class, IdTypeTest.class, MetaTest.class, OrganizationTest.class, ParametersTest.class,
		ParserTest.class, ReferenceTest.class, ReferenceTypTest.class, ResearchStudyTest.class, SerializationTest.class,
		StructureDefinitionTreeTest.class, SubscriptionTest.class, TaskTest.class })
public class TestSuiteHapiLearningAndBugValidationTests
{

}
