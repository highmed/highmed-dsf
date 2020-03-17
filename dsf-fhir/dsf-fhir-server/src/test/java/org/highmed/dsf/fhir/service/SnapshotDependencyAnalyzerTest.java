package org.highmed.dsf.fhir.service;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		try (InputStream in = Files
				.newInputStream(Paths.get("src/test/resources/profiles/DeBasis/patient-de-basis-0.2.1.xml")))
		{
			patientDeBasis = context.newXmlParser().parseResource(StructureDefinition.class, in);
		}
		SnapshotGenerator generator = new SnapshotGeneratorImpl(context,
				new DefaultProfileValidationSupportWithCustomResources());
		patientDeBasis = generator.generateSnapshot(patientDeBasis).getSnapshot();

		SnapshotDependencyAnalyzer analyzer = new SnapshotDependencyAnalyzer();
		SnapshotDependencies dependencies = analyzer.analyzeSnapshotDependencies(patientDeBasis);

		assertNotNull(dependencies);
		logger.debug("Profiles: {}", dependencies.getProfiles());
		logger.debug("TargetProfiles: {}", dependencies.getTargetProfiles());

		assertEquals(2, dependencies.getProfiles().size());
		assertEquals(sorted(Arrays.asList("http://fhir.de/StructureDefinition/address-de-basis",
				"http://fhir.de/StructureDefinition/humanname-de-basis")), sorted(dependencies.getProfiles()));
		assertEquals(
				sorted(Arrays.asList("http://fhir.de/StructureDefinition/organization-de-basis",
						"http://fhir.de/StructureDefinition/practitioner-de-basis",
						"http://hl7.org/fhir/StructureDefinition/Organization",
						"http://hl7.org/fhir/StructureDefinition/Patient",
						"http://hl7.org/fhir/StructureDefinition/RelatedPerson")),
				sorted(dependencies.getTargetProfiles()));
	}

	private List<String> sorted(List<String> list)
	{
		list = new ArrayList<>(list);
		list.sort(String::compareTo);
		return list;
	}
}
