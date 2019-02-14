package org.highmed.fhir.service;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;

public class ResourceValidator
{
	private final FhirValidator validator;

	public ResourceValidator(FhirContext context, StructureDefinition... structureDefinitions)
	{
		validator = configureValidator(context.newValidator(), createValidationSupport(context, structureDefinitions));
	}

	public ValidationResult validate(IBaseResource resource)
	{
		return validator.validateWithResult(resource);
	}

	protected FhirValidator configureValidator(FhirValidator validator, IValidationSupport support)
	{
		FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
		instanceValidator.setValidationSupport(support);

		validator.registerValidatorModule(instanceValidator);
		return validator;
	}

	protected IValidationSupport createValidationSupport(FhirContext context,
			StructureDefinition... structureDefinitions)
	{
		return new DefaultProfileValidationSupportWithCustomStructureDefinitions(context, structureDefinitions);
	}
}
