package org.highmed.fhir.hapi;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.service.DefaultProfileValidationSupportWithCustomResources;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

public class StructureDefinitionTreeTest
{
	@Test
	public void testTree() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Map<String, StructureDefinition> structureDefinitionsByUrl = Files
				.list(Paths.get("src/test/resources/profiles"))
				.map(p -> readStructureDefinition(context, p).setSnapshot(null))
				.collect(Collectors.toMap(StructureDefinition::getUrl, Function.identity()));

		structureDefinitionsByUrl.values().forEach(v -> printTree(v, structureDefinitionsByUrl));
	}

	private void printTree(StructureDefinition def, Map<String, StructureDefinition> structureDefinitionsByUrl)
	{
		System.out.println("\n");

		Set<String> profileDependencies = new HashSet<>();
		Set<String> targetProfileDependencies = new HashSet<>();
		printTree(def.getUrl(), def, structureDefinitionsByUrl, "", profileDependencies, targetProfileDependencies);

		if (!profileDependencies.isEmpty())
		{
			System.out.println("\n  Profile-Dependencies:");
			profileDependencies.stream().sorted().forEach(url -> System.out.println("    " + url));
		}
		if (!targetProfileDependencies.isEmpty())
		{
			System.out.println("\n  TargetProfile-Dependencies:");
			targetProfileDependencies.stream().sorted().forEach(url -> System.out.println("    " + url));
		}
	}

	private void printTree(String k, StructureDefinition def,
			Map<String, StructureDefinition> structureDefinitionsByUrl, String indentation,
			Set<String> profileDependencies, Set<String> targetProfileDependencies)
	{
		System.out.println(indentation + "Profile: " + k);
		for (ElementDefinition element : def.getDifferential().getElement())
		{
			if (element.getType().stream().filter(t -> !t.getProfile().isEmpty() || !t.getTargetProfile().isEmpty())
					.findAny().isPresent())
			{
				System.out
						.println(indentation + "  Element: " + element.getId() + " (Path: " + element.getPath() + ")");
				for (TypeRefComponent type : element.getType())
				{
					if (!type.getProfile().isEmpty())
					{
						for (CanonicalType profile : type.getProfile())
						{
							profileDependencies.add(profile.getValue());

							if (structureDefinitionsByUrl.containsKey(profile.getValue()))
								printTree(profile.getValue(), structureDefinitionsByUrl.get(profile.getValue()),
										structureDefinitionsByUrl, indentation + "    ", profileDependencies,
										targetProfileDependencies);
							else
								System.out.println(indentation + "    Profile: " + profile.getValue() + " ?");
						}
					}
					if (!type.getTargetProfile().isEmpty())
					{
						for (CanonicalType targetProfile : type.getTargetProfile())
						{
							targetProfileDependencies.add(targetProfile.getValue());
							System.out.println(indentation + "    TargetProfile: " + targetProfile.getValue());
						}
					}
				}
			}
		}
	}

	private StructureDefinition readStructureDefinition(FhirContext context, Path p)
	{
		try (InputStream in = Files.newInputStream(p))
		{
			return context.newXmlParser().parseResource(StructureDefinition.class, in);
		}
		catch (DataFormatException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testBuildPatient() throws Exception
	{
		FhirContext context = FhirContext.forR4();

		Map<String, StructureDefinition> structureDefinitionsByUrl = Files
				.list(Paths.get("src/test/resources/profiles"))
				.map(p -> readStructureDefinition(context, p).setSnapshot(null))
				.filter(sd -> sd.getUrl().startsWith("http://fhir.de/"))
				.collect(Collectors.toMap(StructureDefinition::getUrl, Function.identity()));

		StructureDefinition patientDeBasis = structureDefinitionsByUrl
				.get("http://fhir.de/StructureDefinition/patient-de-basis/0.2.1");

		SnapshotGenerator generator = new SnapshotGeneratorImpl(context,
				new DefaultProfileValidationSupportWithCustomResources(structureDefinitionsByUrl.values(),
						Collections.emptyList(), Collections.emptyList()));

		SnapshotWithValidationMessages snapshot = generator.generateSnapshot(patientDeBasis);

		System.out.println(context.newXmlParser().encodeResourceToString(snapshot.getSnapshot()));
		snapshot.getMessages().forEach(vm -> System.out.println(vm.getMessage()));

		assertNotNull(snapshot.getSnapshot());
	}
}
