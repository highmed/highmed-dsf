package org.highmed.dsf.fhir.hapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.highmed.dsf.fhir.service.DefaultProfileValidationSupportWithCustomResources;
import org.highmed.dsf.fhir.service.ResourceValidator;
import org.highmed.dsf.fhir.service.ResourceValidatorImpl;
import org.highmed.dsf.fhir.service.SnapshotGenerator.SnapshotWithValidationMessages;
import org.highmed.dsf.fhir.service.SnapshotGeneratorImpl;
import org.highmed.dsf.fhir.service.StructureDefinitionReader;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class ValidationTest
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationTest.class);

	private static final FhirContext context = FhirContext.forR4();
	private static List<StructureDefinition> snapshots;

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		snapshots = createSnapshots(readStructureDefinitions(context), context);
	}

	private static List<StructureDefinition> createSnapshots(List<StructureDefinition> diffs, FhirContext fhirContext)
	{
		List<StructureDefinition> snapshots = new ArrayList<StructureDefinition>(diffs.size());
		for (StructureDefinition diff : diffs)
		{
			SnapshotGeneratorImpl generator = new SnapshotGeneratorImpl(fhirContext,
					new DefaultProfileValidationSupportWithCustomResources(snapshots, Collections.emptyList(),
							Collections.emptyList()));
			SnapshotWithValidationMessages result = generator.generateSnapshot(diff);
			assertTrue(result.getMessages().isEmpty());

			snapshots.add(result.getSnapshot());
		}

		return snapshots;
	}

	private static List<StructureDefinition> readStructureDefinitions(FhirContext context)
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context);

		return reader.readXml(Paths.get("src/test/resources/profiles/DeBasis/AddressDeBasis.xml"),
				Paths.get("src/test/resources/profiles/DeBasis/patient-de-basis-0.2.1.xml"));
	}

	private Patient createNonValidPatient()
	{
		Patient patient = new Patient();
		patient.getMeta().addProfile("http://fhir.de/StructureDefinition/patient-de-basis/0.2.1");
		patient.getAddressFirstRep().setDistrict("district");
		return patient;
	}

	@Test
	public void testHapiValidation() throws Exception
	{
		FhirValidator validator = context.newValidator();

		FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
		validator.registerValidatorModule(instanceValidator);

		instanceValidator.setValidationSupport(new DefaultProfileValidationSupportWithCustomResources(snapshots,
				Collections.emptyList(), Collections.emptyList()));

		Patient patient = createNonValidPatient();

		ValidationResult result = validator.validateWithResult(patient);

		assertFalse(result.isSuccessful());
		assertEquals(1, result.getMessages().size());
		assertEquals(ResultSeverityEnum.ERROR, result.getMessages().get(0).getSeverity());

		result.getMessages().forEach(r -> logger.info("Validation Issue: {} - {} - {}", r.getSeverity(),
				r.getLocationString(), r.getMessage()));
	}

	@Test
	public void testValidatorImpl()
	{
		ResourceValidator validator = new ResourceValidatorImpl(context,
				new DefaultProfileValidationSupportWithCustomResources(snapshots, Collections.emptyList(),
						Collections.emptyList()));

		ValidationResult result = validator.validate(createNonValidPatient());

		assertFalse(result.isSuccessful());
		assertEquals(1, result.getMessages().size());
		assertEquals(ResultSeverityEnum.ERROR, result.getMessages().get(0).getSeverity());

		result.getMessages().forEach(r -> logger.info("Validation Issue: {} - {} - {}", r.getSeverity(),
				r.getLocationString(), r.getMessage()));
	}
}
