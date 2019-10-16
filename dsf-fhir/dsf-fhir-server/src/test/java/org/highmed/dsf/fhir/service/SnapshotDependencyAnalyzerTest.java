package org.highmed.dsf.fhir.service;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

public class SnapshotDependencyAnalyzerTest
{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotDependencyAnalyzerTest.class);

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
		logger.debug("Profiles: {}", dependencies.getProfiles());
		logger.debug("TargetProfiles: {}", dependencies.getTargetProfiles());
	}
}
