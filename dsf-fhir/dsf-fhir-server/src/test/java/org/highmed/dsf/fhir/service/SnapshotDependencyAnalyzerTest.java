package org.highmed.dsf.fhir.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;

import org.highmed.dsf.fhir.profiles.ValidationSupportRule;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.ClassRule;
import org.junit.Test;

public class SnapshotDependencyAnalyzerTest
{
	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			Arrays.asList("highmed-task-base-0.2.0.xml", "highmed-task-request-simple-feasibility-0.2.0.xml"),
			Collections.emptyList(), Collections.emptyList());

	private SnapshotDependencyAnalyzer analyzer = new SnapshotDependencyAnalyzer();

	@Test
	public void testAnalyze() throws Exception
	{
		SnapshotDependencies dependencies = analyzer.analyzeSnapshotDependencies(
				(StructureDefinition) validationRule.getValidationSupport().fetchStructureDefinition(
						"http://highmed.org/fhir/StructureDefinition/highmed-task-request-simple-feasibility"));

		assertNotNull(dependencies);

		assertEquals(1, dependencies.getProfiles().size());
		assertEquals(18, dependencies.getTargetProfiles().size());
	}
}
