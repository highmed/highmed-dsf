package org.highmed.dsf.fhir.help;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.dao.command.CommandList;
import org.highmed.dsf.fhir.dao.exception.BadBundleException;
import org.highmed.dsf.fhir.dao.exception.ResourceDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.dao.exception.ResourceNotMarkedDeletedException;
import org.highmed.dsf.fhir.dao.exception.ResourceVersionNoMatchException;
import org.highmed.dsf.fhir.function.RunnableWithSqlAndResourceNotFoundException;
import org.highmed.dsf.fhir.function.RunnableWithSqlException;
import org.highmed.dsf.fhir.function.RunnableWithSqlResourceNotFoundAndResourceNotMarkedDeletedException;
import org.highmed.dsf.fhir.function.SupplierWithSqlAndResourceDeletedException;
import org.highmed.dsf.fhir.function.SupplierWithSqlAndResourceNotFoundAndResouceVersionNoMatchException;
import org.highmed.dsf.fhir.function.SupplierWithSqlAndResourceNotFoundException;
import org.highmed.dsf.fhir.function.SupplierWithSqlException;
import org.hl7.fhir.r4.model.IdType;
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

	public WebApplicationException internalServerError(ResourceDeletedException e)
	{
		logger.error("Error while accessing DB", e);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.EXCEPTION,
				"Error while accessing DB");
		return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(outcome).build());
	}

	public WebApplicationException internalServerError(ResourceNotFoundException e)
	{
		logger.error("Error while accessing DB", e);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.EXCEPTION,
				"Error while accessing DB");
		return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(outcome).build());
	}

	public WebApplicationException internalServerErrorBundleTransaction(Exception e)
	{
		logger.error("Error while executing transaction", e);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.EXCEPTION,
				"Error while executing transaction");
		return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(outcome).build());
	}

	public WebApplicationException internalServerErrorBundleBatch(Exception e)
	{
		logger.error("Error while executing batch element", e);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.EXCEPTION,
				"Error while executing batch element");
		return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(outcome).build());
	}

	public <T> T handleSqlExAndResourceNotFoundExAndResouceVersionNonMatchEx(String resourceTypeName,
			SupplierWithSqlAndResourceNotFoundAndResouceVersionNoMatchException<T> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceNotFoundException e)
		{
			throw new WebApplicationException(responseGenerator.updateAsCreateNotAllowed(resourceTypeName, e.getId()));
		}
		catch (ResourceVersionNoMatchException e)
		{
			throw resourceVersionNoMatch(resourceTypeName, e);
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	private WebApplicationException resourceVersionNoMatch(String resourceTypeName, ResourceVersionNoMatchException e)
	{
		logger.error("{} with id {} expected version {} does not match latest version {}", resourceTypeName, e.getId(),
				e.getExpectedVersion(), e.getLatestVersion());

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Resource with id " + e.getId() + " expected version " + e.getExpectedVersion()
						+ " does not match latest version " + e.getLatestVersion());
		return new WebApplicationException(Response.status(Status.PRECONDITION_FAILED).entity(outcome).build());
	}

	public WebApplicationException notFound(String resourceTypeName)
	{
		logger.error("{} with id (not a UUID) not found", resourceTypeName);

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " with id (not a UUID) not found");
		return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(outcome).build());
	}

	public WebApplicationException notFound(String resourceTypeName, ResourceNotFoundException e)
	{
		logger.error("{} with id {} not found", resourceTypeName, e.getId());

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " with id " + e.getId() + " not found");
		return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(outcome).build());
	}

	public <T> T handleSqlAndResourceDeletedException(String serverBase, String resourceTypeName,
			SupplierWithSqlAndResourceDeletedException<T> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceDeletedException e)
		{
			throw gone(serverBase, resourceTypeName, e);
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
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
			throw notFound(resourceTypeName, e);
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	public void handleSqlResourceNotFoundAndResourceNotMarkedDeletedException(String resourceTypeName,
			RunnableWithSqlResourceNotFoundAndResourceNotMarkedDeletedException r)
	{
		try
		{
			r.run();
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
		catch (ResourceNotFoundException e)
		{
			throw notFound(resourceTypeName, e);
		}
		catch (ResourceNotMarkedDeletedException e)
		{
			throw notMarkedDeleted(resourceTypeName, e);
		}
	}

	public WebApplicationException notMarkedDeleted(String resourceTypeName, ResourceNotMarkedDeletedException e)
	{
		logger.warn("{} with id {} is not marked as deleted", resourceTypeName, e.getId());
		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " with id " + e.getId() + " is not marked deleted");
		return new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(outcome).build());
	}

	public WebApplicationException gone(String serverBase, String resourceTypeName, ResourceDeletedException e)
	{
		logger.error("{} with id {} is marked as deleted", resourceTypeName, e.getId());

		OperationOutcome outcome = responseGenerator.createOutcome(IssueSeverity.ERROR, IssueType.DELETED,
				"Resource with id " + e.getId() + " is marked as deleted.");
		EntityTag tag = new EntityTag(e.getId().getVersionIdPart(), true);
		URI location = toUri(serverBase, resourceTypeName, e.getId());
		Date lastModified = Date.from(e.getDeleted().atZone(ZoneId.systemDefault()).toInstant());
		return new WebApplicationException(Response.status(Status.GONE).tag(tag).location(location)
				.lastModified(lastModified).entity(outcome).build());
	}

	private URI toUri(String serverBase, String resourceTypeName, IdType id)
	{
		try
		{
			return new URI(id.withServerBase(serverBase, resourceTypeName).getValue());
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
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

	public CommandList handleBadBundleException(Supplier<CommandList> commandListCreator)
	{
		try
		{
			return commandListCreator.get();
		}
		catch (BadBundleException e)
		{
			logger.warn("Error while creating command list for bundle: {}", e.getMessage());
			throw new WebApplicationException(responseGenerator.badBundleRequest(e.getMessage()));
		}
	}
}
