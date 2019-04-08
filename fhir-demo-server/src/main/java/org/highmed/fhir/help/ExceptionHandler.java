package org.highmed.fhir.help;

import java.sql.SQLException;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.function.RunnableWithSqlAndResourceNotFoundException;
import org.highmed.fhir.function.RunnableWithSqlException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceDeletedException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceNotFoundException;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler
{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

	private final ResponseGenerator responseGenerator;

	public ExceptionHandler(ResponseGenerator responseGenerator)
	{
		this.responseGenerator = responseGenerator;
	}

	public void handleSqlException(RunnableWithSqlException s)
	{
		try
		{
			s.run();
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	public <T> T handleSqlException(SupplierWithSqlException<T> s)
	{
		try
		{
			return s.get();
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	public WebApplicationException internalServerError(SQLException e)
	{
		logger.error("Error while accessing DB", e);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.EXCEPTION,
				"Error while accessing DB");
		return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(outcome).build());
	}

	public <T> T handleSqlAndResourceNotFoundException(String resourceTypeName,
			SupplierWithSqlAndResourceNotFoundException<T> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceNotFoundException e)
		{
			throw methodNotAllowed(resourceTypeName, e);
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	public WebApplicationException methodNotAllowed(String resourceTypeName, ResourceNotFoundException e)
	{
		logger.error("{} with id {} not found", resourceTypeName, e.getId());

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Resource with id " + e.getId() + " not found.");
		return new WebApplicationException(Response.status(Status.METHOD_NOT_ALLOWED).entity(outcome).build());
	}

	public WebApplicationException notFound(String resourceTypeName, IllegalArgumentException e)
	{
		logger.error("{} with id (not a UUID) not found", resourceTypeName);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Resource with id (not a UUID) not found.");
		return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(outcome).build());
	}

	public <T> T handleSqlAndResourceDeletedException(String resourceTypeName,
			SupplierWithSqlAndResourceDeletedException<T> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceDeletedException e)
		{
			throw gone(resourceTypeName, e);
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	public WebApplicationException gone(String resourceTypeName, ResourceDeletedException e)
	{
		logger.error("{} with id {} is marked as deleted", resourceTypeName, e.getId());

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.DELETED,
				"Resource with id " + e.getId() + " is marked as deleted.");
		return new WebApplicationException(Response.status(Status.GONE).entity(outcome).build());
	}

	public WebApplicationException oneAlreadyExists(String resourceTypeName, String ifNoneExistsHeaderValue,
			UriInfo uri)
	{
		logger.info("{} with criteria {} already exists", resourceTypeName, ifNoneExistsHeaderValue);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.INFORMATION, IssueType.DUPLICATE,
				"Resource with criteria '" + ifNoneExistsHeaderValue + "' already exists");

		return new WebApplicationException(Response.status(Status.OK).entity(outcome).build());
	}

	public WebApplicationException multipleAlreadyExists(String resourceTypeName, String ifNoneExistsHeaderValue)
	{
		logger.error("Multiple {} resources with criteria {} already exist", resourceTypeName, ifNoneExistsHeaderValue);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.MULTIPLEMATCHES,
				"Multiple " + resourceTypeName + " resources with criteria '" + ifNoneExistsHeaderValue
						+ "' already exist");
		return new WebApplicationException(Response.status(Status.PRECONDITION_FAILED).entity(outcome).build());
	}

	public WebApplicationException badIfNoneExistHeaderValue(String ifNoneExistsHeaderValue)
	{
		logger.error("Bad If-None-Exist header value '{}'", ifNoneExistsHeaderValue);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad If-None-Exist header value '" + ifNoneExistsHeaderValue + "'");
		return new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(outcome).build());
	}

	public <T> T catchAndLogSqlExceptionAndIfReturn(SupplierWithSqlException<T> s, Supplier<T> onSqlException)
	{
		try
		{
			return s.get();
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing DB", e);
			return onSqlException.get();
		}
	}

	public <T> T catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(SupplierWithSqlAndResourceDeletedException<T> s,
			Supplier<T> onSqlException, Supplier<T> onResourceDeletedException)
	{
		try
		{
			return s.get();
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing DB", e);
			return onSqlException.get();
		}
		catch (ResourceDeletedException e)
		{
			logger.warn("Resource with id " + e.getId() + " marked as deleted.", e);
			return onResourceDeletedException.get();
		}
	}

	public void catchAndLogSqlException(RunnableWithSqlException s)
	{
		try
		{
			s.run();
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing DB", e);
		}
	}

	public void catchAndLogSqlAndResourceNotFoundException(String resourceTypeName,
			RunnableWithSqlAndResourceNotFoundException r)
	{
		try
		{
			r.run();
		}
		catch (ResourceNotFoundException e)
		{
			logger.error(resourceTypeName + " with id " + e.getId() + " not found", e);
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing DB", e);
		}
	}

	public <R> R catchAndLogSqlAndResourceNotFoundException(String resourceTypeName,
			SupplierWithSqlAndResourceNotFoundException<R> s, Supplier<R> onResourceNotFoundException,
			Supplier<R> onSqlException)
	{
		try
		{
			return s.get();
		}
		catch (ResourceNotFoundException e)
		{
			logger.warn(resourceTypeName + " with id " + e.getId() + " not found", e);
			return onResourceNotFoundException.get();
		}
		catch (SQLException e)
		{
			logger.warn("Error while accessing DB", e);
			return onSqlException.get();
		}
	}
}
