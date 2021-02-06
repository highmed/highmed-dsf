package org.highmed.dsf.fhir.dao.command;

import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class ValidationHelperImpl implements ValidationHelper
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationHelperImpl.class);

	private final ResourceValidator resourceValidator;
	private final ResponseGenerator responseGenerator;

	public ValidationHelperImpl(ResourceValidator resourceValidator, ResponseGenerator responseGenerator)
	{
		this.resourceValidator = resourceValidator;
		this.responseGenerator = responseGenerator;
	}

	@Override
	public ValidationResult checkResourceValidForCreate(User user, Resource resource)
	{
		return checkResourceValid(user, resource, "Create");
	}

	@Override
	public ValidationResult checkResourceValidForUpdate(User user, Resource resource)
	{
		return checkResourceValid(user, resource, "Update");
	}

	private ValidationResult checkResourceValid(User user, Resource resource, String method)
	{
		ValidationResult validationResult = resourceValidator.validate(resource);

		if (validationResult.getMessages().stream().anyMatch(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())))
		{
			logger.warn("{} of {} unauthorized, resource not valid: {}", method, resource.fhirType(),
					toValidationLogMessage(validationResult));

			throw new WebApplicationException(
					responseGenerator.forbiddenNotValid(method, user, resource.fhirType(), validationResult));
		}
		else if (!validationResult.getMessages().isEmpty())
			logger.info("Resource {} validated with messages: {}", resource.fhirType(),
					toValidationLogMessage(validationResult));

		return validationResult;
	}

	private String toValidationLogMessage(ValidationResult validationResult)
	{
		return validationResult
				.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
						+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage())
				.collect(Collectors.joining(", ", "[", "]"));
	}
}
