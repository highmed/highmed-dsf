package org.highmed.fhir.webservice.impl;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.AbstractDomainResourceDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.function.SupplierWithSqlException;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
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

public abstract class AbstractServiceImpl<D extends AbstractDomainResourceDao<R>, R extends DomainResource>
		implements BasicService<R>, InitializingBean
{
	protected final String resourceTypeName;
	protected final String serverBase;
	protected final int defaultPageCount;
	protected final D dao;
	protected final ResourceValidator validator;
	protected final EventManager eventManager;
	protected final ExceptionHandler exceptionHandler;
	protected final EventGenerator<R> eventGenerator;
	protected final ResponseGenerator responseGenerator;
	protected final ParameterConverter parameterConverter;

	public AbstractServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount, D dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator<R> eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter)
	{
		this.resourceTypeName = resourceTypeName;
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.dao = dao;
		this.validator = validator;
		this.eventManager = eventManager;
		this.exceptionHandler = exceptionHandler;
		this.eventGenerator = eventGenerator;
		this.responseGenerator = responseGenerator;
		this.parameterConverter = parameterConverter;
	}

	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(validator, "validator");
	}

	@Override
	public Response create(R resource, UriInfo uri, HttpHeaders headers)
	{
		Consumer<R> postCreate = preCreate(resource);

		R createdResource = exceptionHandler.handleSqlException(() -> dao.create(resource));

		eventManager.handleEvent(eventGenerator.newResourceCreatedEvent(createdResource));

		if (postCreate != null)
			postCreate.accept(createdResource);

		URI location = uri.getAbsolutePathBuilder().path("/{id}/_history/{vid}")
				.build(createdResource.getIdElement().getIdPart(), createdResource.getIdElement().getVersionIdPart());

		return responseGenerator.response(Status.CREATED, createdResource, null).location(location).build();
	}

	/**
	 * Override to modify the given resource before db insert, throw {@link WebApplicationException} to interrupt the
	 * normal flow
	 * 
	 * @param resource
	 *            not <code>null</code>
	 * @return if not null, the returned {@link Consumer} will be called after the create operation and before returning
	 *         to the client, the {@link Consumer} can throw a {@link WebApplicationException} to interrupt the normal
	 *         flow, the {@link Consumer} will be called with the created resource
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected Consumer<R> preCreate(R resource) throws WebApplicationException
	{
		return null;
	}

	@Override
	public Response read(String id, UriInfo uri, HttpHeaders headers)
	{
		Optional<R> read = exceptionHandler.handleSqlAndResourceDeletedException(resourceTypeName,
				() -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));

		return read.map(d -> responseGenerator.response(Status.OK, d, parameterConverter.getMediaType(uri, headers)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	@Override
	public Response vread(String id, long version, UriInfo uri, HttpHeaders headers)
	{
		Optional<R> read = exceptionHandler.handleSqlException(vRead(id, version));

		return read.map(d -> responseGenerator.response(Status.OK, d, parameterConverter.getMediaType(uri, headers)))
				.orElse(Response.status(Status.NOT_FOUND)).build();
	}

	private SupplierWithSqlException<Optional<R>> vRead(String id, long version)
	{
		return () -> dao.readVersion(parameterConverter.toUuid(resourceTypeName, id), version);
	}

	@Override
	public Response update(String id, R resource, UriInfo uri, HttpHeaders headers)
	{
		IdType resourceId = resource.getIdElement();

		if (!Objects.equals(id, resourceId.getIdPart()))
			return responseGenerator.createPathVsElementIdResponse(resourceTypeName, id, resourceId);
		if (resourceId.getBaseUrl() != null && !serverBase.equals(resourceId.getBaseUrl()))
			return responseGenerator.createInvalidBaseUrlResponse(resourceTypeName, resourceId);

		Consumer<R> postUpdate = preUpdate(resource);

		R updatedResource = exceptionHandler.handleSqlAndResourceNotFoundException(resourceTypeName,
				() -> dao.update(resource));

		eventManager.handleEvent(eventGenerator.newResourceUpdatedEvent(updatedResource));

		if (postUpdate != null)
			postUpdate.accept(updatedResource);

		return responseGenerator.response(Status.OK, updatedResource, null).build();
	}

	/**
	 * Override to modify the given resource before db update, throw {@link WebApplicationException} to interrupt the
	 * normal flow. Path id vs. resource.id.idPart is checked before this method is called
	 * 
	 * @param resource
	 *            not <code>null</code>
	 * @return if not null, the returned {@link Consumer} will be called after the update operation and before returning
	 *         to the client, the {@link Consumer} can throw a {@link WebApplicationException} to interrupt the normal
	 *         flow, the {@link Consumer} will be called with the updated resource
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected Consumer<R> preUpdate(R resource)
	{
		return null;
	}

	@Override
	public Response delete(String id, UriInfo uri, HttpHeaders headers)
	{
		Consumer<String> afterDelete = beforeDelete(id);

		exceptionHandler.handleSqlException(() -> dao.delete(parameterConverter.toUuid(resourceTypeName, id)));

		eventManager.handleEvent(eventGenerator.newResourceDeletedEvent(id));

		afterDelete.accept(id);

		return responseGenerator.response(Status.OK, null, null).build();
	}

	/**
	 * Override to perform actions pre delete, throw {@link WebApplicationException} to interrupt the normal flow.
	 * 
	 * @param id
	 *            of the resource to be deleted
	 * @return if not null, the returned {@link Consumer} will be called after the create operation and before returning
	 *         to the client, the {@link Consumer} can throw a {@link WebApplicationException} to interrupt the normal
	 *         flow, the {@link Consumer} will be called with the id ({@link IdType#getIdPart()}) of the deleted
	 *         resource
	 * @throws WebApplicationException
	 *             if the normal flow should be interrupted
	 */
	protected Consumer<String> beforeDelete(String id)
	{
		return null;
	}

	@Override
	public Response search(UriInfo uri, HttpHeaders headers)
	{
		MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();

		Integer count = parameterConverter.getFirstInt(queryParameters, "_count");
		Integer page = parameterConverter.getFirstInt(queryParameters, "page");
		String format = queryParameters.getFirst("format");

		int effectivePage = page == null ? 1 : page;
		int effectiveCount = (count == null || count < 0) ? defaultPageCount : count;

		SearchQuery query = dao.createSearchQuery(effectivePage, effectiveCount);

		query.configureParameters(queryParameters);

		PartialResult<R> result = exceptionHandler.handleSqlException(() -> dao.search(query));

		UriBuilder bundleUri = query.configureBundleUri(uri.getAbsolutePathBuilder());

		return responseGenerator.response(Status.OK, responseGenerator.createSearchSet(result, bundleUri, format),
				parameterConverter.getMediaType(uri, headers)).build();
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
	public Response postValidateNew(String validate, Parameters parameters, UriInfo uri, HttpHeaders headers)
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
	public Response getValidateNew(String validate, UriInfo uri, HttpHeaders headers)
	{
		// String mode, String profile from uri
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response postValidateExisting(String validate, Parameters parameters, UriInfo uri, HttpHeaders headers)
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

	@Override
	public Response getValidateExisting(String validate, UriInfo uri, HttpHeaders headers)
	{
		// String mode, String profile from uri
		// TODO Auto-generated method stub
		return null;
	}
}
