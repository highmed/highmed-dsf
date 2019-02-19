package org.highmed.fhir.service;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

public class ResourceValidator
{
	private final FhirValidator validator;

	public ResourceValidator(FhirContext fhirContext, IValidationSupport validationSupport)
	{
		validator = configureValidator(fhirContext.newValidator(), validationSupport);
	}

	public ValidationResult validate(IBaseResource resource)
	{
		return validator.validateWithResult(resource);
	}

	protected FhirValidator configureValidator(FhirValidator validator, IValidationSupport validationSupport)
	{
		FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
		instanceValidator.setValidationSupport(validationSupport);

		validator.registerValidatorModule(instanceValidator);
		return validator;
	}
}
