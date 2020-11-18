package org.highmed.dsf.fhir.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.highmed.dsf.fhir.validation.SnapshotGenerator.SnapshotWithValidationMessages;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
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
				customValidationSupport, new DefaultProfileValidationSupport(context),
				new CommonCodeSystemsTerminologyService(context));

		readProfilesAndGenerateSnapshots(context, customValidationSupport,
				new SnapshotGeneratorImpl(context, validationSupport), structureDefinitions.stream());

		readCodeSystems(context, customValidationSupport, codeSystems.stream());
		readValueSets(context, customValidationSupport, valueSets.stream());
	}

	private static void readProfilesAndGenerateSnapshots(FhirContext context,
			ValidationSupportWithCustomResources vSupport, SnapshotGenerator snapshotGenerator,
			Stream<String> structureDefinitions)
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context);
		reader.readXmlFromClassPath(structureDefinitions.map(file -> "/fhir/StructureDefinition/" + file))
				.forEach(diff ->
				{
					SnapshotWithValidationMessages snapshotWithValidationMessages = snapshotGenerator
							.generateSnapshot(diff);
					assertTrue(snapshotWithValidationMessages.getMessages().isEmpty());
					assertNotNull(snapshotWithValidationMessages.getSnapshot());

					vSupport.addOrReplace(snapshotWithValidationMessages.getSnapshot());
				});
	}

	private static void readCodeSystems(FhirContext context, ValidationSupportWithCustomResources vSupport,
			Stream<String> codeSystems)
	{
		codeSystems.map(file -> "/fhir/CodeSystem/" + file).forEach(file ->
		{
			var cS = readCodeSystem(context, file);
			vSupport.addOrReplace(cS);
		});
	}

	private static CodeSystem readCodeSystem(FhirContext context, String file)
	{
		try (InputStream in = ValidationSupportRule.class.getResourceAsStream(file))
		{
			return context.newXmlParser().parseResource(CodeSystem.class, in);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static void readValueSets(FhirContext context, ValidationSupportWithCustomResources vSupport,
			Stream<String> valueSets)
	{
		valueSets.map(file -> "/fhir/ValueSet/" + file).forEach(file ->
		{
			var vS = readValueSet(context, file);
			vSupport.addOrReplace(vS);
		});
	}

	private static ValueSet readValueSet(FhirContext context, String file)
	{
		try (InputStream in = ValidationSupportRule.class.getResourceAsStream(file))
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
