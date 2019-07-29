package org.highmed.dsf.fhir.service;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;

public class SnapshotDependencyAnalyzerTest
{
	@Test
	public void testAnalyze() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		StructureDefinition patientDeBasis;
		try (InputStream in = Files.newInputStream(Paths.get("src/test/resources/profiles/patient-de-basis-0.2.xml")))
		{
			patientDeBasis = context.newXmlParser().parseResource(StructureDefinition.class, in);
		}

		SnapshotDependencyAnalyzer analyzer = new SnapshotDependencyAnalyzer();
		SnapshotDependencies dependencies = analyzer.analyzeSnapshotDependencies(patientDeBasis);

		assertNotNull(dependencies);
		System.out.println("Profiles: " + dependencies.getProfiles());
		System.out.println("TargetProfiles: " + dependencies.getTargetProfiles());
	}
}
