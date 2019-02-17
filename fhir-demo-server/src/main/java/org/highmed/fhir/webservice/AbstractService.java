package org.highmed.fhir.webservice;

import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.AbstractDomainResourceDao;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.event.ResourceCreatedEvent;
import org.highmed.fhir.event.ResourceDeletedEvent;
import org.highmed.fhir.event.ResourceUpdatedEvent;
import org.highmed.fhir.function.RunnableWithSqlAndResourceNotFoundException;
import org.highmed.fhir.function.RunnableWithSqlException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceDeletedException;
import org.highmed.fhir.function.SupplierWithSqlAndResourceNotFoundException;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.service.ResourceValidator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

@Consumes({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
		Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
public abstract class AbstractService<D extends AbstractDomainResourceDao<R>, R extends DomainResource>
		implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractService.class);

	public static final String JSON_FORMAT = "json";
	public static final List<String> JSON_FORMATS = Arrays.asList(Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW,
			MediaType.APPLICATION_JSON);
	public static final String XML_FORMAT = "xml";
	public static final List<String> XML_FORMATS = Arrays.asList(Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW,
			MediaType.APPLICATION_XML, MediaType.TEXT_XML);

	private final String serverBase;
	private final int defaultPageCount;
	private final Class<R> resourceType;
	private final String resourceTypeName;
	private final D dao;
	private final ResourceValidator validator;
	private final EventManager eventManager;

	public AbstractService(String serverBase, int defaultPageCount, Class<R> resourceType, D dao,
			ResourceValidator validator, EventManager eventManager)
	{
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.resourceType = Objects.requireNonNull(resourceType, "resourceType");
		this.resourceTypeName = resourceType.getAnnotation(ResourceDef.class).name();
		this.dao = dao;
		this.validator = validator;
		this.eventManager = eventManager;
	}

	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(validator, "validator");
	}

	protected final D getDao()
	{
		return dao;
	}

	protected final int getDefaultPageCount()
	{
		return defaultPageCount;
	}

	protected final IdType toFullId(IdType id)
	{
		id.setIdBase(serverBase);
		return id;
	}

	protected final String toFullId(String id)
	{
		return toFullId(new IdType(id)).asStringValue();
	}

	protected OperationOutcome createOutcome(IssueSeverity severity, IssueType type, String diagnostics)
	{
		OperationOutcome outcome = new OperationOutcome();
		outcome.getIssueFirstRep().setSeverity(severity);
		outcome.getIssueFirstRep().setCode(type);
		outcome.getIssueFirstRep().setDiagnostics(diagnostics);
		return outcome;
	}

	protected UUID withUuid(String id)
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

	protected final String toSpecialMimeType(String format)
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

	protected ResponseBuilder response(Status status, Resource resource, String mimeType)
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

	private Integer getFirstInt(MultivaluedMap<String, String> queryParameters, String key)
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

	private Bundle createSearchSet(PartialResult<R> tasks, UriBuilder bundleUri, String format)
	{
		Bundle bundle = new Bundle();
		bundle.setId(UUID.randomUUID().toString());
		bundle.getMeta().setLastUpdated(new Date());
		bundle.setType(BundleType.SEARCHSET);
		bundle.setEntry(tasks.getPartialResult().stream()
				.map(r -> new BundleEntryComponent().setResource(r).setFullUrl(toFullId(r.getId())))
				.collect(Collectors.toList()));
		bundle.setTotal(tasks.getOverallCount());

		if (format != null)
			bundleUri = bundleUri.replaceQueryParam("_format", format);

		if (tasks.getPageAndCount().getCount() > 0)
		{
			bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
			bundleUri = bundleUri.replaceQueryParam("page",
					tasks.getPartialResult().isEmpty() ? 1 : tasks.getPageAndCount().getPage());
		}
		else
			bundleUri = bundleUri.replaceQueryParam("_count", "0");

		bundle.addLink().setRelation("self").setUrlElement(new UriType(bundleUri.build()));

		if (tasks.getPageAndCount().getCount() > 0 && !tasks.getPartialResult().isEmpty())
		{
			bundleUri = bundleUri.replaceQueryParam("page", 1);
			bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
			bundle.addLink().setRelation("first").setUrlElement(new UriType(bundleUri.build()));

			if (tasks.getPageAndCount().getPage() > 1)
			{
				bundleUri = bundleUri.replaceQueryParam("page", tasks.getPageAndCount().getPage() - 1);
				bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
				bundle.addLink().setRelation("previous").setUrlElement(new UriType(bundleUri.build()));
			}
			if (!tasks.isLastPage())
			{
				bundleUri = bundleUri.replaceQueryParam("page", tasks.getPageAndCount().getPage() + 1);
				bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
				bundle.addLink().setRelation("next").setUrlElement(new UriType(bundleUri.build()));
			}

			bundleUri = bundleUri.replaceQueryParam("page", tasks.getLastPage());
			bundleUri = bundleUri.replaceQueryParam("_count", tasks.getPageAndCount().getCount());
			bundle.addLink().setRelation("last").setUrlElement(new UriType(bundleUri.build()));
		}

		return bundle;
	}

	@POST
	public Response create(R resource, @Context UriInfo uri)
	{
		logger.trace("POST '{}'", uri.getRequestUri().toString());

		preCreate(resource);

		R createdResource = handleSqlException(() -> dao.create(resource));

		eventManager.handleEvent(new ResourceCreatedEvent<R>(resourceType, createdResource));

		postCreate(createdResource);

		URI location = uri.getAbsolutePathBuilder().path("/{id}/_history/{vid}")
				.build(createdResource.getIdElement().getIdPart(), createdResource.getIdElement().getVersionIdPart());

		return response(Status.CREATED, createdResource, null).location(location).build();
	}

	/**
	 * Override to modify the given resource before db insert, throw {@link WebApplicationException} to interrupt the
	 * normal flow
	 * 
	 * @param resource
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected void preCreate(R resource) throws WebApplicationException
	{
	}

	/**
	 * Override to modify the created resource before returning to the client, throw {@link WebApplicationException} to
	 * interrupt the normal flow
	 * 
	 * @param createdResource
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected void postCreate(R createdResource) throws WebApplicationException
	{
	}

	@GET
	@Path("/{id}")
	public Response read(@PathParam("id") String id, @QueryParam("_format") String format, @Context UriInfo uri)
	{
		logger.trace("GET '{}'", uri.getRequestUri().toString());

		Optional<R> read = handleSqlAndResourceDeletedException(() -> dao.read(withUuid(id)));

		return read.map(d -> response(Status.OK, d, toSpecialMimeType(format)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	protected void handleSqlException(RunnableWithSqlException s)
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

	protected <T> T handleSqlException(SupplierWithSqlException<T> s)
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

	protected WebApplicationException internalServerError(SQLException e)
	{
		logger.error("Error while accessing DB", e);
		return new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(createOutcome(IssueSeverity.ERROR, IssueType.EXCEPTION, "Error while accessing DB")).build());
	}

	protected <T> T handleSqlAndResourceNotFoundException(SupplierWithSqlAndResourceNotFoundException<T> s)
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

	protected WebApplicationException methodNotAllowed(ResourceNotFoundException e)
	{
		logger.warn(resourceTypeName + " with id {} not found", e.getId());
		return new WebApplicationException(
				Response.status(Status.METHOD_NOT_ALLOWED).entity(createOutcome(IssueSeverity.ERROR,
						IssueType.PROCESSING, "Resource with id " + e.getId() + " not found.")).build());
	}

	protected WebApplicationException notFound(IllegalArgumentException e)
	{
		logger.warn(resourceTypeName + " with id (not a UUID) not found");
		return new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
				createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING, "Resource with id (not a UUID) not found."))
				.build());
	}

	protected <T> T handleSqlAndResourceDeletedException(SupplierWithSqlAndResourceDeletedException<T> s)
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

	protected WebApplicationException gone(ResourceDeletedException e)
	{
		logger.warn(resourceTypeName + " with id {} is marked as deleted", e.getId());
		return new WebApplicationException(Response.status(Status.GONE).entity(createOutcome(IssueSeverity.ERROR,
				IssueType.DELETED, "Resource with id " + e.getId() + " is marked as deleted.")).build());
	}

	protected <T> T catchAndLogSqlExceptionAndIfReturn(SupplierWithSqlException<T> s, Supplier<T> onSqlException)
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

	protected <T> T catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(
			SupplierWithSqlAndResourceDeletedException<T> s, Supplier<T> onSqlException,
			Supplier<T> onResourceDeletedException)
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

	protected void catchAndLogSqlException(RunnableWithSqlException s)
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

	protected void catchAndLogSqlAndResourceNotFoundException(RunnableWithSqlAndResourceNotFoundException s)
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

	@GET
	@Path("/{id}/_history/{vid}")
	public Response vread(@PathParam("id") String id, @PathParam("vid") long version,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		logger.trace("GET '{}'", uri.getRequestUri().toString());

		Optional<R> read = handleSqlException(() -> dao.readVersion(withUuid(id), version));

		return read.map(d -> response(Status.OK, d, toSpecialMimeType(format)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	@PUT
	@Path("/{id}")
	public Response update(@PathParam("id") String id, R resource, @Context UriInfo uri)
	{
		logger.trace("PUT '{}'", uri.getRequestUri().toString());

		IdType resourceId = resource.getIdElement();

		if (!Objects.equals(id, resourceId.getIdPart()))
			return createPathVsElementIdResponse(id, resourceId);
		if (resourceId.getBaseUrl() != null && !serverBase.equals(resourceId.getBaseUrl()))
			return createInvalidBaseUrlResponse(resourceId);

		preUpdate(resource);

		R updatedResource = handleSqlAndResourceNotFoundException(() -> dao.update(resource));

		eventManager.handleEvent(new ResourceUpdatedEvent<R>(resourceType, updatedResource));

		postUpdate(updatedResource);

		return response(Status.OK, updatedResource, null).build();
	}

	/**
	 * Override to modify the given resource before db update, throw {@link WebApplicationException} to interrupt the
	 * normal flow. Path id vs. resource.id.idPart is checked before this method is called
	 * 
	 * @param resource
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected void preUpdate(R resource)
	{
	}

	/**
	 * Override to modify the updated resource before returning to the client, throw {@link WebApplicationException} to
	 * interrupt the normal flow
	 * 
	 * @param updatedResource
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected void postUpdate(R updatedResource)
	{
	}

	private Response createPathVsElementIdResponse(String id, IdType resourceId)
	{
		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Path id not equal to " + resourceTypeName + " id (" + id + "vs." + resourceId.getIdPart() + ").");
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	private Response createInvalidBaseUrlResponse(IdType resourceId)
	{
		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " id.baseUrl must be null or equal to " + serverBase + ", value "
						+ resourceId.getBaseUrl() + " unexpected.");
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") String id, @Context UriInfo uri)
	{
		logger.trace("DELETE '{}'", uri.getRequestUri().toString());

		preDelete(id);

		handleSqlException(() -> dao.delete(withUuid(id)));

		eventManager.handleEvent(new ResourceDeletedEvent<R>(resourceType, id));

		postDelete(id);

		return response(Status.OK, null, null).build();
	}

	/**
	 * Override to perform actions pre delete, throw {@link WebApplicationException} to interrupt the normal flow.
	 * 
	 * @param id
	 *            of the resource to be deleted
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected void preDelete(String id)
	{
	}

	/**
	 * Override to perform actions post delete, this method should not throw {@link WebApplicationException}
	 * 
	 * @param id
	 *            of the deleted resource
	 */
	protected void postDelete(String id)
	{
	}

	@GET
	public Response search(@Context UriInfo uri)
	{
		logger.trace("GET {}", uri.getRequestUri().toString());

		MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();

		Integer count = getFirstInt(queryParameters, "_count");
		Integer page = getFirstInt(queryParameters, "page");
		String format = queryParameters.getFirst("format");

		int effectivePage = page == null ? 1 : page;
		int effectiveCount = (count == null || count < 0) ? getDefaultPageCount() : count;

		/* SearchParameter implementations are not thread safe and need to be created on a request basis */
		SearchQuery query = getDao().createSearchQuery(effectivePage, effectiveCount);

		query.configureParameters(queryParameters);

		PartialResult<R> result = handleSqlException(() -> dao.search(query));

		UriBuilder bundleUri = uri.getAbsolutePathBuilder();
		query.configureBundleUri(bundleUri);

		return response(Status.OK, createSearchSet(result, bundleUri, format), toSpecialMimeType(format)).build();
	}

	private Optional<Resource> getResource(Parameters parameters, String parameterName)
	{
		return parameters.getParameter().stream().filter(p -> parameterName.equals(p.getName())).findFirst()
				.map(ParametersParameterComponent::getResource);
	}

	protected OperationOutcome createValidationOutcome(List<ValidationMessage> messages)
	{
		OperationOutcome outcome = new OperationOutcome();
		List<OperationOutcomeIssueComponent> issues = messages.stream().map(vm -> new OperationOutcomeIssueComponent()
				.setSeverity(IssueSeverity.ERROR).setCode(IssueType.STRUCTURE).setDiagnostics(vm.getMessage()))
				.collect(Collectors.toList());
		outcome.setIssue(issues);
		return outcome;
	}

	@POST
	@Path("/{validate : [$]validate(/)?}")
	public Response validateNew(@PathParam("validate") String validate, Parameters parameters, @Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		Optional<Resource> resource = getResource(parameters, "resource");
		if (resource.isEmpty())
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome, hint post with id url?

		Type mode = parameters.getParameter("mode");
		if (!(mode instanceof CodeType))
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome

		Type profile = parameters.getParameter("profile");
		if (!(profile instanceof UriType))
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome

		// TODO handle mode and profile parameters

		// ValidationResult validationResult = validator.validate(resource.get());

		// TODO create return values

		return Response.ok().build();
	}

	@POST
	@Path("/{id}/{validate : [$]validate(/)?}")
	public Response validateExisting(@PathParam("validate") String validate, Parameters parameters,
			@Context UriInfo uri)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		if (getResource(parameters, "resource").isPresent())
			return Response.status(Status.BAD_REQUEST)
					.build(); /* TODO return OperationOutcome, hint post without id url */

		Type mode = parameters.getParameter("mode");
		if (!(mode instanceof CodeType))
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome

		Type profile = parameters.getParameter("profile");
		if (!(profile instanceof UriType))
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome

		return Response.ok().build();
	}
}
