package org.highmed.dsf.fhir.webservice.impl;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.command.CheckReferencesCommand;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.history.HistoryService;
import org.highmed.dsf.fhir.prefer.PreferHandlingType;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.base.AbstractBasicService;
import org.highmed.dsf.fhir.webservice.specification.BasicResourceService;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public abstract class AbstractResourceServiceImpl<D extends ResourceDao<R>, R extends Resource>
		extends AbstractBasicService implements BasicResourceService<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceImpl.class);

	private final String path;
	protected final Class<R> resourceType;
	protected final String resourceTypeName;
	protected final String serverBase;
	protected final int defaultPageCount;
	protected final D dao;
	protected final ResourceValidator validator;
	protected final EventHandler eventHandler;
	protected final ExceptionHandler exceptionHandler;
	protected final EventGenerator eventGenerator;
	protected final ResponseGenerator responseGenerator;
	protected final ParameterConverter parameterConverter;
	protected final ReferenceExtractor referenceExtractor;
	protected final ReferenceResolver referenceResolver;
	protected final ReferenceCleaner referenceCleaner;
	protected final AuthorizationRuleProvider authorizationRuleProvider;
	protected final HistoryService historyService;

	public AbstractResourceServiceImpl(String path, Class<R> resourceType, String serverBase, int defaultPageCount,
			D dao, ResourceValidator validator, EventHandler eventHandler, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, AuthorizationRuleProvider authorizationRuleProvider,
			HistoryService historyService)
	{
		this.path = path;
		this.resourceType = resourceType;
		this.resourceTypeName = resourceType.getAnnotation(ResourceDef.class).name();
		this.serverBase = serverBase;
		this.defaultPageCount = defaultPageCount;
		this.dao = dao;
		this.validator = validator;
		this.eventHandler = eventHandler;
		this.exceptionHandler = exceptionHandler;
		this.eventGenerator = eventGenerator;
		this.responseGenerator = responseGenerator;
		this.parameterConverter = parameterConverter;
		this.referenceExtractor = referenceExtractor;
		this.referenceResolver = referenceResolver;
		this.referenceCleaner = referenceCleaner;
		this.authorizationRuleProvider = authorizationRuleProvider;
		this.historyService = historyService;
	}

	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(resourceTypeName, "resourceTypeName");
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(defaultPageCount, "defaultPageCount");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(validator, "validator");
		Objects.requireNonNull(eventHandler, "eventHandler");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(eventGenerator, "eventGenerator");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
		Objects.requireNonNull(referenceExtractor, "referenceExtractor");
		Objects.requireNonNull(referenceResolver, "referenceResolver");
		Objects.requireNonNull(referenceCleaner, "referenceCleaner");
		Objects.requireNonNull(authorizationRuleProvider, "authorizationRuleProvider");
		Objects.requireNonNull(historyService, "historyService");
	}

	@Override
	public Response create(R resource, UriInfo uri, HttpHeaders headers)
	{
		checkAlreadyExists(headers); // might throw errors

		Consumer<R> afterCreate = preCreate(resource);

		R createdResource = exceptionHandler.handleSqlException(() ->
		{
			try (Connection connection = dao.newReadWriteTransaction())
			{
				try
				{
					resolveLogicalReferences(resource, connection);

					R created = dao.createWithTransactionAndId(connection, resource, UUID.randomUUID());

					checkReferences(resource, connection, ref -> checkReferenceAfterCreate(resource, ref));

					connection.commit();

					return created;
				}
				catch (SQLException | WebApplicationException e)
				{
					connection.rollback();
					throw e;
				}
			}
		});

		referenceCleaner.cleanLiteralReferences(createdResource);

		eventHandler.handleEvent(eventGenerator.newResourceCreatedEvent(createdResource));

		if (afterCreate != null)
			afterCreate.accept(createdResource);

		URI location = toLocation(createdResource);

		return responseGenerator.response(Status.CREATED, createdResource,
				parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers),
				parameterConverter.getPreferReturn(headers), () -> responseGenerator.created(location, createdResource))
				.location(location).lastModified(createdResource.getMeta().getLastUpdated())
				.tag(new EntityTag(createdResource.getMeta().getVersionId(), true)).build();
	}

	/**
	 * <i>Override this method to exclude references from being checked after a create, add similar rule to
	 * {@link CheckReferencesCommand}</i>
	 *
	 * @param created
	 *            not <code>null</code>
	 * @param ref
	 *            not <code>null</code>
	 * @return true if a reference should be checked
	 * @see CheckReferencesCommand
	 */
	protected boolean checkReferenceAfterCreate(R created, ResourceReference ref)
	{
		return true;
	}

	private URI toLocation(R resource)
	{
		return UriBuilder.fromUri(serverBase).path(resource.getResourceType().name())
				.path("/{id}/" + Constants.PARAM_HISTORY + "/{vid}")
				.build(resource.getIdElement().getIdPart(), resource.getIdElement().getVersionIdPart());
	}

	private void resolveLogicalReferences(Resource resource, Connection connection) throws WebApplicationException
	{
		referenceExtractor.getReferences(resource).filter(ref -> ReferenceType.LOGICAL.equals(ref.getType(serverBase)))
				.filter(ref -> referenceResolver.referenceCanBeResolved(ref, connection)).forEach(ref ->
				{
					Optional<OperationOutcome> outcome = resolveLogicalReference(resource, ref, connection);
					if (outcome.isPresent())
					{
						Response response = Response.status(Status.FORBIDDEN).entity(outcome.get()).build();
						throw new WebApplicationException(response);
					}
				});
	}

	private Optional<OperationOutcome> resolveLogicalReference(Resource resource, ResourceReference reference,
			Connection connection)
	{
		Optional<Resource> resolvedResource = referenceResolver.resolveReference(getCurrentUser(), reference,
				connection);
		if (resolvedResource.isPresent())
		{
			Resource target = resolvedResource.get();
			reference.getReference().setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));

			return Optional.empty();
		}
		else
			return Optional.of(responseGenerator.referenceTargetNotFoundLocallyByIdentifier(resource, reference));
	}

	private void checkReferences(Resource resource, Connection connection, Predicate<ResourceReference> checkReference)
			throws WebApplicationException
	{
		referenceExtractor.getReferences(resource).filter(checkReference)
				.filter(ref -> referenceResolver.referenceCanBeChecked(ref, connection)).forEach(ref ->
				{
					Optional<OperationOutcome> outcome = checkReference(resource, connection, ref);
					if (outcome.isPresent())
					{
						Response response = Response.status(Status.FORBIDDEN).entity(outcome.get()).build();
						throw new WebApplicationException(response);
					}
				});
	}

	private Optional<OperationOutcome> checkReference(Resource resource, Connection connection,
			ResourceReference reference) throws WebApplicationException
	{
		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case LITERAL_INTERNAL:
			case RELATED_ARTEFACT_LITERAL_INTERNAL_URL:
			case ATTACHMENT_LITERAL_INTERNAL_URL:
				return referenceResolver.checkLiteralInternalReference(resource, reference, connection);
			case LITERAL_EXTERNAL:
			case RELATED_ARTEFACT_LITERAL_EXTERNAL_URL:
			case ATTACHMENT_LITERAL_EXTERNAL_URL:
				return referenceResolver.checkLiteralExternalReference(resource, reference);
			case LOGICAL:
				return referenceResolver.checkLogicalReference(getCurrentUser(), resource, reference, connection);
			// unknown urls to non FHIR servers in related artifacts must not be checked
			case RELATED_ARTEFACT_UNKNOWN_URL:
			case ATTACHMENT_UNKNOWN_URL:
				return Optional.empty();
			case UNKNOWN:
			default:
				return Optional.of(responseGenerator.unknownReference(resource, reference));
		}
	}

	private void checkAlreadyExists(HttpHeaders headers) throws WebApplicationException
	{
		Optional<String> ifNoneExistHeader = getHeaderString(headers, Constants.HEADER_IF_NONE_EXIST,
				Constants.HEADER_IF_NONE_EXIST_LC);

		if (ifNoneExistHeader.isEmpty())
			return; // header not found, nothing to check against

		if (ifNoneExistHeader.get().isBlank())
			throw new WebApplicationException(responseGenerator.badIfNoneExistHeaderValue(ifNoneExistHeader.get()));

		String ifNoneExistHeaderValue = ifNoneExistHeader.get();
		if (!ifNoneExistHeaderValue.contains("?"))
			ifNoneExistHeaderValue = '?' + ifNoneExistHeaderValue;

		UriComponents componentes = UriComponentsBuilder.fromUriString(ifNoneExistHeaderValue).build();
		String path = componentes.getPath();
		if (path != null && !path.isBlank())
			throw new WebApplicationException(responseGenerator.badIfNoneExistHeaderValue(ifNoneExistHeader.get()));

		Map<String, List<String>> queryParameters = parameterConverter
				.urlDecodeQueryParameters(componentes.getQueryParams());
		if (Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(queryParameters::containsKey))
		{
			logger.warn(
					"{} Header contains query parameter not applicable in this conditional create context: '{}', parameters {} will be ignored",
					Constants.HEADER_IF_NONE_EXIST, ifNoneExistHeader.get(),
					Arrays.toString(SearchQuery.STANDARD_PARAMETERS));

			queryParameters = queryParameters.entrySet().stream()
					.filter(e -> !Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(p -> p.equals(e.getKey())))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		}

		SearchQuery<R> query = dao.createSearchQueryWithoutUserFilter(1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
			throw new WebApplicationException(
					responseGenerator.badIfNoneExistHeaderValue(ifNoneExistHeader.get(), unsupportedQueryParameters));

		PartialResult<R> result = exceptionHandler.handleSqlException(() -> dao.search(query));
		if (result.getTotal() == 1)
			throw new WebApplicationException(
					responseGenerator.oneExists(result.getPartialResult().get(0), ifNoneExistHeader.get()));
		else if (result.getTotal() > 1)
			throw new WebApplicationException(
					responseGenerator.multipleExists(resourceTypeName, ifNoneExistHeader.get()));
	}

	private Optional<String> getHeaderString(HttpHeaders headers, String... headerNames)
	{
		return Arrays.stream(headerNames).map(name -> headers.getHeaderString(name)).filter(h -> h != null).findFirst();
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
		Optional<R> read = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
				() -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));

		Optional<EntityTag> ifNoneMatch = getHeaderString(headers, Constants.HEADER_IF_NONE_MATCH,
				Constants.HEADER_IF_NONE_MATCH_LC).flatMap(parameterConverter::toEntityTag);
		Optional<Date> ifModifiedSince = getHeaderString(headers, Constants.HEADER_IF_MODIFIED_SINCE,
				Constants.HEADER_IF_MODIFIED_SINCE_LC).flatMap(this::toDate);

		return read.map(resource ->
		{
			referenceCleaner.cleanLiteralReferences(resource);

			EntityTag resourceTag = new EntityTag(resource.getMeta().getVersionId(), true);
			if (ifNoneMatch.map(t -> t.equals(resourceTag)).orElse(false))
				return Response.notModified(resourceTag).lastModified(resource.getMeta().getLastUpdated()).build();

			// If-Modified-Since is ignored, when used in combination with If-None-Match
			else if (ifNoneMatch.isEmpty() && ifModifiedSince
					.map(d -> !afterWithSecondsPrecision(resource.getMeta().getLastUpdated(), d)).orElse(false))
				return Response.notModified(resourceTag).lastModified(resource.getMeta().getLastUpdated()).build();
			else
				return responseGenerator.response(Status.OK, resource, getMediaTypeForRead(uri, headers)).build();
		}).orElseGet(() -> Response.status(Status.NOT_FOUND).build()); // TODO return OperationOutcome
	}

	private boolean afterWithSecondsPrecision(Date a, Date b)
	{
		LocalDateTime aLdt = a.toInstant().atZone(ZoneOffset.UTC.normalized()).toLocalDateTime()
				.truncatedTo(ChronoUnit.SECONDS);
		LocalDateTime bLdt = b.toInstant().atZone(ZoneOffset.UTC.normalized()).toLocalDateTime()
				.truncatedTo(ChronoUnit.SECONDS);

		return aLdt.isAfter(bLdt);
	}

	protected MediaType getMediaTypeForRead(UriInfo uri, HttpHeaders headers)
	{
		return parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers);
	}

	/**
	 * @param rfc1123DateValue
	 *            RFC 1123 date string
	 * @return {@link Optional} of {@link Date} in system default timezone or {@link Optional#empty()} if the given
	 *         value could not be parsed or was null/blank
	 */
	private Optional<Date> toDate(String rfc1123DateValue)
	{
		if (rfc1123DateValue == null || rfc1123DateValue.isBlank())
			return Optional.empty();

		try
		{
			ZonedDateTime parsed = ZonedDateTime.parse(rfc1123DateValue,
					DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault()));
			return Optional.of(Date.from(parsed.toInstant()));
		}
		catch (DateTimeParseException e)
		{
			logger.warn("Not a RFC-1123 date", e);
			return Optional.empty();
		}
	}

	@Override
	public Response vread(String id, long version, UriInfo uri, HttpHeaders headers)
	{
		Optional<R> read = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, id,
				() -> dao.readVersion(parameterConverter.toUuid(resourceTypeName, id), version));

		Optional<EntityTag> ifNoneMatch = getHeaderString(headers, Constants.HEADER_IF_NONE_MATCH,
				Constants.HEADER_IF_NONE_MATCH_LC).flatMap(parameterConverter::toEntityTag);
		Optional<Date> ifModifiedSince = getHeaderString(headers, Constants.HEADER_IF_MODIFIED_SINCE,
				Constants.HEADER_IF_MODIFIED_SINCE_LC).flatMap(this::toDate);

		return read.map(resource ->
		{
			referenceCleaner.cleanLiteralReferences(resource);

			EntityTag resourceTag = new EntityTag(resource.getMeta().getVersionId(), true);
			if (ifNoneMatch.map(t -> t.equals(resourceTag)).orElse(false))
				return Response.notModified(resourceTag).lastModified(resource.getMeta().getLastUpdated()).build();

			// If-Modified-Since is ignored, when used in combination with If-None-Match
			else if (ifNoneMatch.isEmpty() && ifModifiedSince
					.map(d -> !afterWithSecondsPrecision(resource.getMeta().getLastUpdated(), d)).orElse(false))
				return Response.notModified(resourceTag).lastModified(resource.getMeta().getLastUpdated()).build();
			else
				return responseGenerator.response(Status.OK, resource, getMediaTypeForVRead(uri, headers)).build();
		}).orElseGet(() -> Response.status(Status.NOT_FOUND).build()); // TODO return OperationOutcome
	}

	protected MediaType getMediaTypeForVRead(UriInfo uri, HttpHeaders headers)
	{
		return parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers);
	}

	@Override
	public Response history(UriInfo uri, HttpHeaders headers)
	{
		Bundle history = historyService.getHistory(getCurrentUser(), uri, headers, resourceType);

		return responseGenerator.response(Status.OK, referenceCleaner.cleanLiteralReferences(history),
				parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
	}

	@Override
	public Response history(String id, UriInfo uri, HttpHeaders headers)
	{
		Bundle history = historyService.getHistory(getCurrentUser(), uri, headers, resourceType, id);

		return responseGenerator
				.response(Status.OK, history, parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
	}

	@Override
	public Response update(String id, R resource, UriInfo uri, HttpHeaders headers)
	{
		IdType resourceId = resource.getIdElement();

		if (!Objects.equals(id, resourceId.getIdPart()))
			return responseGenerator.pathVsElementId(resourceTypeName, id, resourceId);
		if (resourceId.getBaseUrl() != null && !serverBase.equals(resourceId.getBaseUrl()))
			return responseGenerator.invalidBaseUrl(resourceTypeName, resourceId);

		Consumer<R> afterUpdate = preUpdate(resource);

		Optional<Long> ifMatch = getHeaderString(headers, Constants.HEADER_IF_MATCH, Constants.HEADER_IF_MATCH_LC)
				.flatMap(parameterConverter::toEntityTag).flatMap(parameterConverter::toVersion);

		R updatedResource = exceptionHandler
				.handleSqlExAndResourceNotFoundExAndResouceVersionNonMatchEx(resourceTypeName, () ->
				{
					try (Connection connection = dao.newReadWriteTransaction())
					{
						try
						{
							resolveLogicalReferences(resource, connection);

							R updated = dao.update(resource, ifMatch.orElse(null));

							checkReferences(resource, connection, ref -> checkReferenceAfterUpdate(updated, ref));

							connection.commit();

							return updated;
						}
						catch (SQLException | WebApplicationException e)
						{
							connection.rollback();
							throw e;
						}
					}
				});

		referenceCleaner.cleanLiteralReferences(updatedResource);

		eventHandler.handleEvent(eventGenerator.newResourceUpdatedEvent(updatedResource));

		if (afterUpdate != null)
			afterUpdate.accept(updatedResource);

		URI location = toLocation(updatedResource);

		return responseGenerator
				.response(Status.OK, updatedResource, parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers),
						parameterConverter.getPreferReturn(headers),
						() -> responseGenerator.updated(location, updatedResource))
				.location(location).lastModified(updatedResource.getMeta().getLastUpdated())
				.tag(new EntityTag(updatedResource.getMeta().getVersionId(), true)).build();
	}

	/**
	 * <i>Override this method to exclude references from being checked after an update, add similar rule to
	 * {@link CheckReferencesCommand}</i>
	 *
	 * @param updated
	 *            not <code>null</code>
	 * @param ref
	 *            not <code>null</code>
	 * @return true if a reference should be checked
	 * @see CheckReferencesCommand
	 */
	protected boolean checkReferenceAfterUpdate(R updated, ResourceReference ref)
	{
		return true;
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
	public Response update(R resource, UriInfo uri, HttpHeaders headers)
	{
		throw new UnsupportedOperationException("Implemented and delegated by security layer");
	}

	@Override
	public Response delete(String id, UriInfo uri, HttpHeaders headers)
	{
		Consumer<String> afterDelete = preDelete(id);

		boolean deleted = exceptionHandler.handleSqlAndResourceNotFoundException(resourceTypeName,
				() -> dao.delete(parameterConverter.toUuid(resourceTypeName, id)));

		if (deleted)
			eventHandler.handleEvent(eventGenerator.newResourceDeletedEvent(resourceType, id));

		if (afterDelete != null)
			afterDelete.accept(id);

		return responseGenerator.response(Status.OK, responseGenerator.resourceDeleted(resourceTypeName, id),
				parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
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
	protected Consumer<String> preDelete(String id)
	{
		return null;
	}

	@Override
	public Response delete(UriInfo uri, HttpHeaders headers)
	{
		throw new UnsupportedOperationException("Implemented and delegated by security layer");
	}

	@Override
	public Response search(UriInfo uri, HttpHeaders headers)
	{
		MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();

		Integer page = parameterConverter.getFirstInt(queryParameters, SearchQuery.PARAMETER_PAGE);
		int effectivePage = page == null ? 1 : page;

		Integer count = parameterConverter.getFirstInt(queryParameters, SearchQuery.PARAMETER_COUNT);
		int effectiveCount = (count == null || count < 0) ? defaultPageCount : count;

		SearchQuery<R> query = dao.createSearchQuery(getCurrentUser(), effectivePage, effectiveCount);
		query.configureParameters(queryParameters);
		List<SearchQueryParameterError> errors = query.getUnsupportedQueryParameters(queryParameters);

		// if query parameter errors and client requests strict handling -> bad request outcome
		if (!errors.isEmpty() && PreferHandlingType.STRICT.equals(parameterConverter.getPreferHandling(headers)))
			return responseGenerator.response(Status.BAD_REQUEST, responseGenerator.toOperationOutcomeError(errors),
					parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();

		PartialResult<R> result = exceptionHandler.handleSqlException(() -> dao.search(query));

		result = filterIncludeResources(result);

		UriBuilder bundleUri = query.configureBundleUri(UriBuilder.fromPath(serverBase).path(path));

		String format = queryParameters.getFirst(SearchQuery.PARAMETER_FORMAT);
		String pretty = queryParameters.getFirst(SearchQuery.PARAMETER_PRETTY);
		Bundle searchSet = responseGenerator.createSearchSet(result, errors, bundleUri, format, pretty);

		// clean literal references from bundle entries
		searchSet.getEntry().stream().filter(BundleEntryComponent::hasResource).map(BundleEntryComponent::getResource)
				.forEach(referenceCleaner::cleanLiteralReferences);

		return responseGenerator
				.response(Status.OK, searchSet, parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers))
				.build();
	}

	private PartialResult<R> filterIncludeResources(PartialResult<R> result)
	{
		List<Resource> includes = filterIncludeResources(result.getIncludes());
		return new PartialResult<R>(result.getTotal(), result.getPageAndCount(), result.getPartialResult(), includes);
	}

	private List<Resource> filterIncludeResources(List<Resource> includes)
	{
		return includes.stream().filter(this::filterIncludeResource).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private boolean filterIncludeResource(Resource include)
	{
		Optional<AuthorizationRule<? extends Resource>> optRule = authorizationRuleProvider
				.getAuthorizationRule(include.getClass());

		return optRule.map(rule -> (AuthorizationRule<Resource>) rule)
				.flatMap(rule -> rule.reasonReadAllowed(getCurrentUser(), include)).map(reason ->
				{
					logger.debug("Include resource of type {} with id {}, allowed - {}",
							include.getClass().getAnnotation(ResourceDef.class).name(),
							include.getIdElement().getValue(), reason);
					return true;
				}).orElseGet(() ->
				{
					logger.debug("Include resource of type {} with id {}, filtered (read not allowed)",
							include.getClass().getAnnotation(ResourceDef.class).name(),
							include.getIdElement().getValue());
					return false;
				});
	}

	private Optional<Resource> getResource(Parameters parameters, String parameterName)
	{
		return parameters.getParameter().stream().filter(p -> parameterName.equals(p.getName())).findFirst()
				.map(ParametersParameterComponent::getResource);
	}

	private OperationOutcome createValidationOutcomeError(List<SingleValidationMessage> messages)
	{
		OperationOutcome outcome = new OperationOutcome();
		List<OperationOutcomeIssueComponent> issues = messages.stream().map(vm -> new OperationOutcomeIssueComponent()
				.setSeverity(toSeverity(vm.getSeverity())).setCode(IssueType.STRUCTURE).setDiagnostics(vm.getMessage()))
				.collect(Collectors.toList());
		outcome.setIssue(issues);
		return outcome;
	}

	private IssueSeverity toSeverity(ResultSeverityEnum resultSeverity)
	{
		switch (resultSeverity)
		{
			case ERROR:
				return IssueSeverity.ERROR;
			case FATAL:
				return IssueSeverity.FATAL;
			case INFORMATION:
				return IssueSeverity.INFORMATION;
			case WARNING:
				return IssueSeverity.WARNING;
			default:
				return IssueSeverity.NULL;
		}
	}

	private OperationOutcome createValidationOutcomeOk(List<SingleValidationMessage> messages, List<String> profiles)
	{
		OperationOutcome outcome = new OperationOutcome();

		List<OperationOutcomeIssueComponent> issues = messages.stream().map(vm -> new OperationOutcomeIssueComponent()
				.setSeverity(toSeverity(vm.getSeverity())).setCode(IssueType.STRUCTURE).setDiagnostics(vm.getMessage()))
				.collect(Collectors.toList());
		outcome.setIssue(issues);

		OperationOutcomeIssueComponent ok = new OperationOutcomeIssueComponent().setSeverity(IssueSeverity.INFORMATION)
				.setCode(IssueType.INFORMATIONAL)
				.setDiagnostics("Resource validated" + (profiles.isEmpty() ? ""
						: " with profile" + (profiles.size() > 1 ? "s " : " ")
								+ profiles.stream().collect(Collectors.joining(", "))));
		outcome.addIssue(ok);

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
		// MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();
		//
		// String mode = queryParameters.getFirst("mode");
		// String profile = queryParameters.getFirst("profile");

		// mode = create

		// TODO Auto-generated method stub
		return Response.ok().build();
	}

	@Override
	public Response postValidateExisting(String validate, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers)
	{
		if (getResource(parameters, "resource").isPresent())
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome

		Type mode = parameters.getParameter("mode");
		if (!(mode instanceof CodeType) || !(mode instanceof StringType))
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome
		if (!"profile".equals(((StringType) mode).getValue()))
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome

		Type profile = parameters.getParameter("profile");
		if (!(profile instanceof UriType) || !(mode instanceof UrlType) || !(mode instanceof CanonicalType))
			return Response.status(Status.BAD_REQUEST).build(); // TODO return OperationOutcome

		UriType profileUri = (UriType) profile;

		Optional<R> read = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
				() -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));

		R resource = read.get();
		resource.getMeta().setProfile(Collections.singletonList(new CanonicalType(profileUri.getValue())));

		ValidationResult result = validator.validate(resource);

		if (result.isSuccessful())
			return responseGenerator.response(Status.OK,
					createValidationOutcomeOk(result.getMessages(), Collections.singletonList(profileUri.getValue())),
					parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
		else
			return responseGenerator.response(Status.OK, createValidationOutcomeError(result.getMessages()),
					parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
	}

	@Override
	public Response getValidateExisting(String validate, String id, UriInfo uri, HttpHeaders headers)
	{
		MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();

		String mode = queryParameters.getFirst("mode");
		if (mode == null)
			mode = "profile";
		String profile = queryParameters.getFirst("profile");

		if ("profile".equals(mode))
		{
			Optional<R> read = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
					() -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));

			R resource = read.get();
			if (profile != null)
				resource.getMeta().setProfile(Collections.singletonList(new CanonicalType(profile)));

			ValidationResult result = validator.validate(resource);

			if (result.isSuccessful())
				return responseGenerator.response(Status.OK,
						createValidationOutcomeOk(result.getMessages(),
								resource.getMeta().getProfile().stream().map(t -> t.getValue())
										.collect(Collectors.toList())),
						parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
			else
				return responseGenerator.response(Status.OK, createValidationOutcomeError(result.getMessages()),
						parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
		}
		else if ("delete".equals(mode))
			return Response.status(Status.METHOD_NOT_ALLOWED).build(); // TODO mode = delete
		else
			return Response.status(Status.METHOD_NOT_ALLOWED).build(); // TODO return OperationOutcome
	}

	@Override
	public Response deletePermanently(String deletePath, String id, UriInfo uri, HttpHeaders headers)
	{
		exceptionHandler.handleSqlResourceNotFoundAndResourceNotMarkedDeletedException(resourceTypeName,
				() -> dao.deletePermanently(parameterConverter.toUuid(resourceTypeName, id)));

		return responseGenerator.response(Status.OK, responseGenerator.resourceDeletedPermanently(resourceTypeName, id),
				parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
	}
}
