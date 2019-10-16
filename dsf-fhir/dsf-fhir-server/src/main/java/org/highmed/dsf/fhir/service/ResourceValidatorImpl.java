package org.highmed.dsf.fhir.service;

import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

public class ResourceValidatorImpl implements ResourceValidator
{
	private final FhirValidator validator;

	public ResourceValidatorImpl(FhirContext context, IValidationSupport validationSupport)
	{
		this.validator = configureValidator(context.newValidator(), validationSupport);
	}

	protected FhirValidator configureValidator(FhirValidator validator, IValidationSupport validationSupport)
	{
		FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
		instanceValidator.setValidationSupport(validationSupport);

		validator.registerValidatorModule(instanceValidator);
		return validator;
	}

	@Override
	public ValidationResult validate(Resource resource)
	{
		return validator.validateWithResult(resource);
	}
}
