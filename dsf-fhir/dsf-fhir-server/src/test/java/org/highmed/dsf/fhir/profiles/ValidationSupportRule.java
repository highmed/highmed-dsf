package org.highmed.dsf.fhir.profiles;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.service.ValidationSupportWithCustomResources;
import org.highmed.dsf.fhir.service.SnapshotGenerator;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.service.StructureDefinitionReader;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.i18n.HapiLocalizer;
import ca.uhn.fhir.validation.ValidationResult;

public class ValidationSupportRule extends ExternalResource
{
	private static final String BASE_FOLDER = "src/main/resources/fhir/";
	private static final String STUCTURE_DEFINITIONS_FOLDER = BASE_FOLDER + "StructureDefinition";
	private static final String CODE_SYSTEMS_FOLDER = BASE_FOLDER + "CodeSystem";
	private static final String VALUE_SETS_FOLDER = BASE_FOLDER + "ValueSet";

	private final FhirContext context;
	private final IValidationSupport validationSupport;

	public ValidationSupportRule(List<String> structureDefinitions, List<String> codeSystems, List<String> valueSets)
	{
		context = FhirContext.forR4();
		HapiLocalizer localizer = new HapiLocalizer()
		{
			@Override
			public Locale getLocale()
			{
				return Locale.ROOT;
			}
		};
		context.setLocalizer(localizer);

		var customValidationSupport = new ValidationSupportWithCustomResources(context);

		validationSupport = new ValidationSupportChain(new InMemoryTerminologyServerValidationSupport(context),
				customValidationSupport, new DefaultProfileValidationSupport(context));

		readProfilesAndGenerateSnapshots(context, customValidationSupport,
				new SnapshotGeneratorImpl(context, validationSupport),
				toPaths(STUCTURE_DEFINITIONS_FOLDER, structureDefinitions));

		readCodeSystems(context, customValidationSupport, toPaths(CODE_SYSTEMS_FOLDER, codeSystems));
		readValueSets(context, customValidationSupport, toPaths(VALUE_SETS_FOLDER, valueSets));
	}

	private static Stream<Path> toPaths(String folder, List<String> files)
	{
		return files.stream().map(file -> Paths.get(folder, file));
	}

	private static void readProfilesAndGenerateSnapshots(FhirContext context,
			ValidationSupportWithCustomResources vSupport, SnapshotGenerator snapshotGenerator,
			Stream<Path> structureDefinitions)
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context);
		reader.readXml(structureDefinitions).forEach(diff ->
		{
			SnapshotWithValidationMessages snapshotWithValidationMessages = snapshotGenerator.generateSnapshot(diff);
			assertTrue(snapshotWithValidationMessages.getMessages().isEmpty());
			assertNotNull(snapshotWithValidationMessages.getSnapshot());

			vSupport.addOrReplace(snapshotWithValidationMessages.getSnapshot());
		});
	}

	private static void readCodeSystems(FhirContext context, ValidationSupportWithCustomResources vSupport,
			Stream<Path> codeSystems)
	{
		codeSystems.forEach(path ->
		{
			var cS = readCodeSystem(context, path);
			vSupport.addOrReplace(cS);
		});
	}

	private static CodeSystem readCodeSystem(FhirContext context, Path path)
	{
		try (InputStream in = Files.newInputStream(path))
		{
			return context.newXmlParser().parseResource(CodeSystem.class, in);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static void readValueSets(FhirContext context, ValidationSupportWithCustomResources vSupport,
			Stream<Path> valueSets)
	{
		valueSets.forEach(p ->
		{
			var vS = readValueSet(context, p);
			vSupport.addOrReplace(vS);
		});
	}

	private static ValueSet readValueSet(FhirContext context, Path path)
	{
		try (InputStream in = Files.newInputStream(path))
		{
			return context.newXmlParser().parseResource(ValueSet.class, in);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public FhirContext getFhirContext()
	{
		return context;
	}

	public IValidationSupport getValidationSupport()
	{
		return validationSupport;
	}

	public static void logValidationMessages(Logger logger, ValidationResult result)
	{
		result.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
				+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage()).forEach(logger::info);
	}
}
