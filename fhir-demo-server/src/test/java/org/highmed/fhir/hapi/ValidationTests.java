package org.highmed.fhir.hapi;

import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;
import java.util.List;

import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public class ValidationTests
{
	@Test
	public void testSnapshot() throws Exception
	{
		// IWorkerContext context = new SimpleWorkerContext();
		// List<ValidationMessage> messages = null;
		// ProfileKnowledgeProvider pkp = null;
		// ProfileUtilities profileUtilities = new ProfileUtilities(context, messages, pkp);
		// StructureDefinition base = null;
		// StructureDefinition derived = null;
		// String url = null;
		// String profileName = null;
		// profileUtilities.generateSnapshot(base, derived, url, profileName);
	}

	@Test
	public void testValidation() throws Exception
	{

		FhirContext context = FhirContext.forR4();
		FhirValidator validator = context.newValidator();

		FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
		validator.registerValidatorModule(instanceValidator);

		List<StructureDefinition> sD = ValidationSupport.readStructureDefinitions(context,
				Paths.get("src/test/resources/patient-de-basis-0.2.xml"),
				Paths.get("src/test/resources/address-de-basis-0.2.xml"));
		ValidationSupportChain support = new ValidationSupportChain(new ValidationSupport(context, sD));

		instanceValidator.setValidationSupport(support);

		Patient patient = new Patient();
		patient.getMeta().addProfile("http://fhir.de/StructureDefinition/patient-de-basis/0.2");
		patient.getAddressFirstRep().setDistrict("district");

		ValidationResult result = validator.validateWithResult(patient);

		assertFalse(result.isSuccessful());

		System.out.println(result.isSuccessful());
		for (SingleValidationMessage next : result.getMessages())
		{
			System.out.println(
					" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
		}
	}
}
