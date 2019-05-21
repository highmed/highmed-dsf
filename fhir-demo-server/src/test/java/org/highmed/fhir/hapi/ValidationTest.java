package org.highmed.fhir.hapi;

import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.highmed.fhir.service.DefaultProfileValidationSupportWithCustomResources;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.service.ResourceValidatorImpl;
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

		instanceValidator.setValidationSupport(new DefaultProfileValidationSupportWithCustomResources(
				readStructureDefinitions(context), Collections.emptyList(), Collections.emptyList()));

		Patient patient = createNonValidPatient();

		ValidationResult result = validator.validateWithResult(patient);

		assertFalse(result.isSuccessful());

		result.getMessages().forEach(r -> logger.info("Validation Issue: {} - {} - {}", r.getSeverity(),
				r.getLocationString(), r.getMessage()));
	}

	private List<StructureDefinition> readStructureDefinitions(FhirContext context)
	{
		StructureDefinitionReader reader = new StructureDefinitionReader(context);

		return reader.readXml(Paths.get("src/test/resources/profiles/patient-de-basis-0.2.xml"),
				Paths.get("src/test/resources/profiles/address-de-basis-0.2.xml"));
	}

	private Patient createNonValidPatient()
	{
		Patient patient = new Patient();
		patient.getMeta().addProfile("http://fhir.de/StructureDefinition/patient-de-basis/0.2");
		patient.getAddressFirstRep().setDistrict("district");
		return patient;
	}

	@Test
	public void testNonValidPatient()
	{
		FhirContext context = FhirContext.forR4();
		List<StructureDefinition> readStructureDefinitions = readStructureDefinitions(context);
		ResourceValidator validator = new ResourceValidatorImpl(context,
				new DefaultProfileValidationSupportWithCustomResources(readStructureDefinitions,
						Collections.emptyList(), Collections.emptyList()));

		ValidationResult result = validator.validate(createNonValidPatient());
		assertFalse(result.isSuccessful());
	}
}
