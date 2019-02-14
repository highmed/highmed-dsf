package org.highmed.fhir.hapi;

import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;

import org.highmed.fhir.service.DefaultProfileValidationSupportWithCustomStructureDefinitions;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.service.StructureDefinitionReader;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

public class ValidationTest
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationTest.class);

	@Test
	public void testValidation() throws Exception
	{
		FhirContext context = FhirContext.forR4();
		FhirValidator validator = context.newValidator();

		FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
		validator.registerValidatorModule(instanceValidator);

		instanceValidator.setValidationSupport(new DefaultProfileValidationSupportWithCustomStructureDefinitions(
				context, readStructureDefinitions(context)));

		Patient patient = createPatient();

		ValidationResult result = validator.validateWithResult(patient);

		assertFalse(result.isSuccessful());

		result.getMessages().forEach(r -> logger.info("Validation Issue: {} - {} - {}", r.getSeverity(),
				r.getLocationString(), r.getMessage()));
	}

	private StructureDefinition[] readStructureDefinitions(FhirContext context)
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context);

		return reader.readXml(Paths.get("src/test/resources/patient-de-basis-0.2.xml"),
				Paths.get("src/test/resources/address-de-basis-0.2.xml"));
	}

	private Patient createPatient()
	{
		Patient patient = new Patient();
		patient.getMeta().addProfile("http://fhir.de/StructureDefinition/patient-de-basis/0.2");
		patient.getAddressFirstRep().setDistrict("district");
		return patient;
	}

	@Test
	public void testValidator()
	{
		FhirContext context = FhirContext.forR4();
		ResourceValidator validator = new ResourceValidator(context, readStructureDefinitions(context));

		ValidationResult result = validator.validate(createPatient());
		assertFalse(result.isSuccessful());
	}
}
