package org.highmed.dsf.fhir.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.highmed.dsf.fhir.validation.SnapshotGenerator.SnapshotWithValidationMessages;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.r4.model.ActivityDefinition;
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
	private static final String VERSION_PATTERN_STRING1 = "#{version}";
	private static final Pattern VERSION_PATTERN1 = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING1));
	// ${...} pattern to be backwards compatible
	private static final String VERSION_PATTERN_STRING2 = "${version}";
	private static final Pattern VERSION_PATTERN2 = Pattern.compile(Pattern.quote(VERSION_PATTERN_STRING2));

	private static final String DATE_PATTERN_STRING1 = "#{date}";
	private static final Pattern DATE_PATTERN1 = Pattern.compile(Pattern.quote(DATE_PATTERN_STRING1));
	// ${...} pattern to be backwards compatible
	private static final String DATE_PATTERN_STRING2 = "${date}";
	private static final Pattern DATE_PATTERN2 = Pattern.compile(Pattern.quote(DATE_PATTERN_STRING2));
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final String version;
	private final LocalDate date;

	private final FhirContext context;
	private final IValidationSupport validationSupport;

	public ValidationSupportRule(List<String> structureDefinitions, List<String> codeSystems, List<String> valueSets)
	{
		this(null, structureDefinitions, codeSystems, valueSets);
	}

	public ValidationSupportRule(String version, List<String> structureDefinitions, List<String> codeSystems,
			List<String> valueSets)
	{
		this(version, LocalDate.MIN, structureDefinitions, codeSystems, valueSets);
	}

	public ValidationSupportRule(String version, LocalDate date, List<String> structureDefinitions,
			List<String> codeSystems, List<String> valueSets)
	{
		this.version = version;
		this.date = date;

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

		readProfilesAndGenerateSnapshots(context, version, date, customValidationSupport,
				new SnapshotGeneratorImpl(context, validationSupport), structureDefinitions.stream());

		readCodeSystems(context, version, date, customValidationSupport, codeSystems.stream());
		readValueSets(context, version, date, customValidationSupport, valueSets.stream());
	}

	private static void readProfilesAndGenerateSnapshots(FhirContext context, String version, LocalDate date,
			ValidationSupportWithCustomResources vSupport, SnapshotGenerator snapshotGenerator,
			Stream<String> structureDefinitions)
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context, version, date);
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

	private static void readCodeSystems(FhirContext context, String version, LocalDate date,
			ValidationSupportWithCustomResources vSupport, Stream<String> codeSystems)
	{
		codeSystems.map(file -> "/fhir/CodeSystem/" + file).forEach(file ->
		{
			var cS = readCodeSystem(context, version, date, file);
			vSupport.addOrReplace(cS);
		});
	}

	private static CodeSystem readCodeSystem(FhirContext context, String version, LocalDate date, String file)
	{
		try (InputStream in = ValidationSupportRule.class.getResourceAsStream(file))
		{
			if (in == null)
				throw new IOException("File " + file + " not found");

			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = replaceVersionAndDate(read, version, date);

			return context.newXmlParser().parseResource(CodeSystem.class, read);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static void readValueSets(FhirContext context, String version, LocalDate date,
			ValidationSupportWithCustomResources vSupport, Stream<String> valueSets)
	{
		valueSets.map(file -> "/fhir/ValueSet/" + file).forEach(file ->
		{
			var vS = readValueSet(context, version, date, file);
			vSupport.addOrReplace(vS);
		});
	}

	private static ValueSet readValueSet(FhirContext context, String version, LocalDate date, String file)
	{
		try (InputStream in = ValidationSupportRule.class.getResourceAsStream(file))
		{
			if (in == null)
				throw new IOException("File " + file + " not found");

			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = replaceVersionAndDate(read, version, date);

			return context.newXmlParser().parseResource(ValueSet.class, read);
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

	private static String replaceVersionAndDate(String read, String version, LocalDate date)
	{
		read = VERSION_PATTERN1.matcher(read).replaceAll(version);
		read = VERSION_PATTERN2.matcher(read).replaceAll(version);

		if (date != null && !LocalDate.MIN.equals(date))
		{
			String dateValue = date.format(DATE_FORMAT);
			read = DATE_PATTERN1.matcher(read).replaceAll(dateValue);
			read = DATE_PATTERN2.matcher(read).replaceAll(dateValue);
		}
		return read;
	}

	public ActivityDefinition readActivityDefinition(Path file) throws IOException
	{
		try (InputStream in = Files.newInputStream(file))
		{
			String read = IOUtils.toString(in, StandardCharsets.UTF_8);
			read = replaceVersionAndDate(read, version, date);

			return context.newXmlParser().parseResource(ActivityDefinition.class, read);
		}
	}
}
