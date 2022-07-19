package org.highmed.dsf.fhir.validation;

import java.util.regex.Pattern;

import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class ResourceValidatorImpl implements ResourceValidator
{
	private static final Pattern AT_DEFAULT_SLICE_PATTERN = Pattern
			.compile(".*(Questionnaire|QuestionnaireResponse).item:@default.*");

	private final FhirValidator validator;

	public ResourceValidatorImpl(FhirContext context, IValidationSupport validationSupport)
	{
		this.validator = configureValidator(context.newValidator(), validationSupport);
	}

	protected FhirValidator configureValidator(FhirValidator validator, IValidationSupport validationSupport)
	{
		FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
		validator.registerValidatorModule(instanceValidator);
		return validator;
	}

	@Override
	public ValidationResult validate(Resource resource)
	{
		ValidationResult result = validator.validateWithResult(resource);

		// TODO: remove after HAPI validator is fixed: https://github.com/hapifhir/org.hl7.fhir.core/issues/193
		adaptDefaultSliceValidationErrorToWarning(result);

		return result;
	}

	private void adaptDefaultSliceValidationErrorToWarning(ValidationResult result)
	{
		result.getMessages().stream().filter(m -> AT_DEFAULT_SLICE_PATTERN.matcher(m.getMessage()).matches())
				.forEach(m -> m.setSeverity(ResultSeverityEnum.WARNING));
	}
}
