package org.highmed.dsf.fhir.exception;

import java.util.Objects;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.parser.DataFormatException;

@Provider
public class DataFormatExceptionHandler implements ExceptionMapper<DataFormatException>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DataFormatExceptionHandler.class);

	private final ResponseGenerator responseGenerator;

	public DataFormatExceptionHandler(ResponseGenerator responseGenerator)
	{
		this.responseGenerator = responseGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(responseGenerator, "responseGenerator");
	}

	@Override
	public Response toResponse(DataFormatException exception)
	{
		logger.warn("Error while parsing resource: {}, returning OperationOutcome with status 403 Forbidden",
				exception.getMessage());

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.STRUCTURE,
				"Unable to parse resource");
		return Response.status(Status.FORBIDDEN).entity(outcome).build();
	}
}
