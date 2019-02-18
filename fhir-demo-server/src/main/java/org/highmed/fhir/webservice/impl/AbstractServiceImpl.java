package org.highmed.fhir.webservice.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.AbstractDomainResourceDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.BasicService;
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
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.api.Constants;

public abstract class AbstractServiceImpl<D extends AbstractDomainResourceDao<R>, R extends DomainResource>
		implements BasicService<R>, InitializingBean
{
	public static final String JSON_FORMAT = "json";
	public static final List<String> JSON_FORMATS = Arrays.asList(Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW,
			MediaType.APPLICATION_JSON);
	public static final String XML_FORMAT = "xml";
	public static final List<String> XML_FORMATS = Arrays.asList(Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW,
			MediaType.APPLICATION_XML, MediaType.TEXT_XML);

	protected final String serverBase;
	protected final int defaultPageCount;
	protected final D dao;
	protected final ResourceValidator validator;
	protected final EventManager eventManager;
	protected final ServiceHelperImpl<R> serviceHelper;

	public AbstractServiceImpl(String serverBase, int defaultPageCount, D dao, ResourceValidator validator,
			EventManager eventManager, ServiceHelperImpl<R> serviceHelper)
	{
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.dao = dao;
		this.validator = validator;
		this.eventManager = eventManager;
		this.serviceHelper = serviceHelper;
	}

	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(validator, "validator");
	}

	@Override
	public Response create(R resource, @Context UriInfo uri)
	{
		preCreate(resource);

		R createdResource = serviceHelper.handleSqlException(() -> dao.create(resource));

		eventManager.handleEvent(serviceHelper.newResourceCreatedEvent(resource));

		postCreate(createdResource);

		URI location = uri.getAbsolutePathBuilder().path("/{id}/_history/{vid}")
				.build(createdResource.getIdElement().getIdPart(), createdResource.getIdElement().getVersionIdPart());

		return serviceHelper.response(Status.CREATED, createdResource, null).location(location).build();
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

	@Override
	public Response read(@PathParam("id") String id, @QueryParam("_format") String format, @Context UriInfo uri)
	{
		Optional<R> read = serviceHelper
				.handleSqlAndResourceDeletedException(() -> dao.read(serviceHelper.withUuid(id)));

		return read.map(d -> serviceHelper.response(Status.OK, d, serviceHelper.toSpecialMimeType(format)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	@Override
	public Response vread(@PathParam("id") String id, @PathParam("vid") long version,
			@QueryParam("_format") String format, @Context UriInfo uri)
	{
		Optional<R> read = serviceHelper.handleSqlException(vRead(id, version));

		return read.map(d -> serviceHelper.response(Status.OK, d, serviceHelper.toSpecialMimeType(format)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	private SupplierWithSqlException<Optional<R>> vRead(String id, long version)
	{
		return () -> dao.readVersion(serviceHelper.withUuid(id), version);
	}

	@Override
	public Response update(@PathParam("id") String id, R resource, @Context UriInfo uri)
	{
		IdType resourceId = resource.getIdElement();

		if (!Objects.equals(id, resourceId.getIdPart()))
			return serviceHelper.createPathVsElementIdResponse(id, resourceId);
		if (resourceId.getBaseUrl() != null && !serverBase.equals(resourceId.getBaseUrl()))
			return serviceHelper.createInvalidBaseUrlResponse(resourceId);

		preUpdate(resource);

		R updatedResource = serviceHelper.handleSqlAndResourceNotFoundException(() -> dao.update(resource));

		eventManager.handleEvent(serviceHelper.newResourceUpdatedEvent(updatedResource));

		postUpdate(updatedResource);

		return serviceHelper.response(Status.OK, updatedResource, null).build();
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

	@Override
	public Response delete(@PathParam("id") String id, @Context UriInfo uri)
	{
		preDelete(id);

		serviceHelper.handleSqlException(() -> dao.delete(serviceHelper.withUuid(id)));

		eventManager.handleEvent(serviceHelper.newResourceDeletedEvent(id));

		postDelete(id);

		return serviceHelper.response(Status.OK, null, null).build();
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

	@Override
	public Response search(@Context UriInfo uri)
	{
		MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();

		Integer count = serviceHelper.getFirstInt(queryParameters, "_count");
		Integer page = serviceHelper.getFirstInt(queryParameters, "page");
		String format = queryParameters.getFirst("format");

		int effectivePage = page == null ? 1 : page;
		int effectiveCount = (count == null || count < 0) ? defaultPageCount : count;

		/* SearchParameter implementations are not thread safe and need to be created on a request basis */
		SearchQuery query = dao.createSearchQuery(effectivePage, effectiveCount);

		query.configureParameters(queryParameters);

		PartialResult<R> result = serviceHelper.handleSqlException(() -> dao.search(query));

		UriBuilder bundleUri = uri.getAbsolutePathBuilder();
		query.configureBundleUri(bundleUri);

		return serviceHelper.response(Status.OK, serviceHelper.createSearchSet(result, bundleUri, format),
				serviceHelper.toSpecialMimeType(format)).build();
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

	@Override
	public Response validateNew(@PathParam("validate") String validate, Parameters parameters, @Context UriInfo uri)
	{
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

	@Override
	public Response validateExisting(@PathParam("validate") String validate, Parameters parameters,
			@Context UriInfo uri)
	{
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
