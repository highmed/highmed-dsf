package org.highmed.fhir.webservice.impl;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.event.ResourceCreatedEvent;
import org.highmed.fhir.event.ResourceDeletedEvent;
import org.highmed.fhir.event.ResourceUpdatedEvent;
import org.highmed.fhir.function.RunnableWithSqlAndResourceNotFoundException;
import org.highmed.fhir.function.RunnableWithSqlException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceDeletedException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceNotFoundException;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.highmed.fhir.search.PartialResult;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

public class ServiceHelperImpl<R extends DomainResource> implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ServiceHelperImpl.class);

	public static final String JSON_FORMAT = "json";
	public static final List<String> JSON_FORMATS = Arrays.asList(Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW,
			MediaType.APPLICATION_JSON);
	public static final String XML_FORMAT = "xml";
	public static final List<String> XML_FORMATS = Arrays.asList(Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW,
			MediaType.APPLICATION_XML, MediaType.TEXT_XML);

	private final String serverBase;
	private final Class<R> resourceType;
	private final String resourceTypeName;

	public ServiceHelperImpl(String serverBase, Class<R> resourceType)
	{
		this.serverBase = serverBase;
		this.resourceType = resourceType;
		this.resourceTypeName = resourceType.getAnnotation(ResourceDef.class).name();
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(resourceTypeName, "resourceTypeName");
	}

	public Class<R> getResourceType()
	{
		return resourceType;
	}

	public OperationOutcome createOutcome(IssueSeverity severity, IssueType type, String diagnostics)
	{
		OperationOutcome outcome = new OperationOutcome();
		outcome.getIssueFirstRep().setSeverity(severity);
		outcome.getIssueFirstRep().setCode(type);
		outcome.getIssueFirstRep().setDiagnostics(diagnostics);
		return outcome;
	}

	public UUID withUuid(String id)
	{
		if (id == null)
			return null;

		// TODO control flow by exception
		try
		{
			return UUID.fromString(id);
		}
		catch (IllegalArgumentException e)
		{
			throw notFound(e);
		}
	}

	public String toSpecialMimeType(String format)
	{
		if (format == null || format.isBlank())
			return null;
		if (XML_FORMATS.contains(format) || JSON_FORMATS.contains(format))
			return format;
		else if (XML_FORMAT.equals(format))
			return Constants.CT_FHIR_XML_NEW;
		else if (JSON_FORMAT.equals(format))
			return Constants.CT_FHIR_JSON_NEW;
		else
			throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
	}

	public ResponseBuilder response(Status status, Resource resource, String mimeType)
	{
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(resource, "resource");

		ResponseBuilder b = Response.status(status).entity(resource);

		if (mimeType != null)
			b = b.type(mimeType);

		if (resource.getMeta() != null)
		{
			b = b.lastModified(resource.getMeta().getLastUpdated());
			b = b.tag(resource.getMeta().getVersionId());
		}

		return b;
	}

	public Integer getFirstInt(MultivaluedMap<String, String> queryParameters, String key)
	{
		String first = queryParameters.getFirst(key);
		try
		{
			return Integer.valueOf(first);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	public IdType toFullId(IdType id)
	{
		id.setIdBase(serverBase);
		return id;
	}

	public String toFullId(String id)
	{
		return toFullId(new IdType(id)).asStringValue();
	}

	public BundleEntryComponent toBundleEntryComponent(DomainResource resource)
	{
		return new BundleEntryComponent().setResource(resource).setFullUrl(toFullId(resource.getId()));
	}

	public Bundle createSearchSet(PartialResult<? extends DomainResource> result, UriBuilder bundleUri, String format)
	{
		Bundle bundle = new Bundle();
		bundle.setId(UUID.randomUUID().toString());
		bundle.getMeta().setLastUpdated(new Date());
		bundle.setType(BundleType.SEARCHSET);
		result.getPartialResult().stream().map(this::toBundleEntryComponent).forEach(bundle::addEntry);

		bundle.setTotal(result.getOverallCount());

		if (format != null)
			bundleUri = bundleUri.replaceQueryParam("_format", format);

		if (result.getPageAndCount().getCount() > 0)
		{
			bundleUri = bundleUri.replaceQueryParam("_count", result.getPageAndCount().getCount());
			bundleUri = bundleUri.replaceQueryParam("page",
					result.getPartialResult().isEmpty() ? 1 : result.getPageAndCount().getPage());
		}
		else
			bundleUri = bundleUri.replaceQueryParam("_count", "0");

		bundle.addLink().setRelation("self").setUrlElement(new UriType(bundleUri.build()));

		if (result.getPageAndCount().getCount() > 0 && !result.getPartialResult().isEmpty())
		{
			bundleUri = bundleUri.replaceQueryParam("page", 1);
			bundleUri = bundleUri.replaceQueryParam("_count", result.getPageAndCount().getCount());
			bundle.addLink().setRelation("first").setUrlElement(new UriType(bundleUri.build()));

			if (result.getPageAndCount().getPage() > 1)
			{
				bundleUri = bundleUri.replaceQueryParam("page", result.getPageAndCount().getPage() - 1);
				bundleUri = bundleUri.replaceQueryParam("_count", result.getPageAndCount().getCount());
				bundle.addLink().setRelation("previous").setUrlElement(new UriType(bundleUri.build()));
			}
			if (!result.isLastPage())
			{
				bundleUri = bundleUri.replaceQueryParam("page", result.getPageAndCount().getPage() + 1);
				bundleUri = bundleUri.replaceQueryParam("_count", result.getPageAndCount().getCount());
				bundle.addLink().setRelation("next").setUrlElement(new UriType(bundleUri.build()));
			}

			bundleUri = bundleUri.replaceQueryParam("page", result.getLastPage());
			bundleUri = bundleUri.replaceQueryParam("_count", result.getPageAndCount().getCount());
			bundle.addLink().setRelation("last").setUrlElement(new UriType(bundleUri.build()));
		}

		return bundle;
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
		return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(createOutcome(IssueSeverity.ERROR, IssueType.EXCEPTION, "Error while accessing DB")).build());
	}

	public <T> T handleSqlAndResourceNotFoundException(SupplierWithSqlAndResourceNotFoundException<T> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceNotFoundException e)
		{
			throw methodNotAllowed(e);
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	public WebApplicationException methodNotAllowed(ResourceNotFoundException e)
	{
		logger.warn(resourceTypeName + " with id {} not found", e.getId());
		return new WebApplicationException(
				Response.status(Status.METHOD_NOT_ALLOWED).entity(createOutcome(IssueSeverity.ERROR,
						IssueType.PROCESSING, "Resource with id " + e.getId() + " not found.")).build());
	}

	public WebApplicationException notFound(IllegalArgumentException e)
	{
		logger.warn(resourceTypeName + " with id (not a UUID) not found");
		return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
				createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING, "Resource with id (not a UUID) not found."))
				.build());
	}

	public <T> T handleSqlAndResourceDeletedException(SupplierWithSqlAndResourceDeletedException<T> s)
	{
		try
		{
			return s.get();
		}
		catch (ResourceDeletedException e)
		{
			throw gone(e);
		}
		catch (SQLException e)
		{
			throw internalServerError(e);
		}
	}

	public WebApplicationException gone(ResourceDeletedException e)
	{
		logger.warn(resourceTypeName + " with id {} is marked as deleted", e.getId());
		return new WebApplicationException(Response.status(Status.GONE).entity(createOutcome(IssueSeverity.ERROR,
				IssueType.DELETED, "Resource with id " + e.getId() + " is marked as deleted.")).build());
	}

	public <T> T catchAndLogSqlExceptionAndIfReturn(SupplierWithSqlException<T> s, Supplier<T> onSqlException)
	{
		try
		{
			return s.get();
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing DB", e);
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

	public void catchAndLogSqlAndResourceNotFoundException(RunnableWithSqlAndResourceNotFoundException s)
	{
		try
		{
			s.run();
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

	public Response createPathVsElementIdResponse(String id, IdType resourceId)
	{
		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Path id not equal to " + resourceTypeName + " id (" + id + "vs." + resourceId.getIdPart() + ").");
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response createInvalidBaseUrlResponse(IdType resourceId)
	{
		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " id.baseUrl must be null or equal to " + serverBase + ", value "
						+ resourceId.getBaseUrl() + " unexpected.");
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Optional<Resource> getResource(Parameters parameters, String parameterName)
	{
		return parameters.getParameter().stream().filter(p -> parameterName.equals(p.getName())).findFirst()
				.map(ParametersParameterComponent::getResource);
	}

	public OperationOutcome createValidationOutcome(List<ValidationMessage> messages)
	{
		OperationOutcome outcome = new OperationOutcome();
		List<OperationOutcomeIssueComponent> issues = messages.stream().map(vm -> new OperationOutcomeIssueComponent()
				.setSeverity(IssueSeverity.ERROR).setCode(IssueType.STRUCTURE).setDiagnostics(vm.getMessage()))
				.collect(Collectors.toList());
		outcome.setIssue(issues);
		return outcome;
	}

	public ResourceCreatedEvent<R> newResourceCreatedEvent(R createdResource)
	{
		return new ResourceCreatedEvent<R>(resourceType, createdResource);
	}

	public ResourceUpdatedEvent<R> newResourceUpdatedEvent(R updatedResource)
	{
		return new ResourceUpdatedEvent<R>(resourceType, updatedResource);
	}

	public ResourceDeletedEvent<R> newResourceDeletedEvent(String id)
	{
		return new ResourceDeletedEvent<R>(resourceType, id);
	}
}
