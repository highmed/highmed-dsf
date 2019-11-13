package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventManager;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class UpdateCommand<R extends Resource, D extends ResourceDao<R>> extends AbstractCommandWithResource<R, D>
		implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateCommand.class);

	protected final ResponseGenerator responseGenerator;
	protected final EventManager eventManager;
	protected final EventGenerator eventGenerator;

	protected UUID id;
	protected R updatedResource;

	public UpdateCommand(int index, Bundle bundle, BundleEntryComponent entry, String serverBase, R resource, D dao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			ResponseGenerator responseGenerator, EventManager eventManager, EventGenerator eventGenerator)
	{
		super(3, index, bundle, entry, serverBase, resource, dao, exceptionHandler, parameterConverter);

		this.responseGenerator = responseGenerator;
		this.eventManager = eventManager;
		this.eventGenerator = eventGenerator;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable)
	{
		UriComponents componentes = UriComponentsBuilder.fromUriString(entry.getRequest().getUrl()).build();
		if (componentes.getPathSegments().size() == 2 && componentes.getQueryParams().isEmpty())
		{
			if (entry.getFullUrl().startsWith(URL_UUID_PREFIX))
				throw new WebApplicationException(
						responseGenerator.badUpdateRequestUrl(index, entry.getRequest().getUrl()));
			else if (!resource.hasIdElement() || !resource.getIdElement().hasIdPart())
				throw new WebApplicationException(
						responseGenerator.bundleEntryResouceMissingId(index, resource.getResourceType().name()));
			else if (resource.getIdElement().getIdPart().startsWith(URL_UUID_PREFIX))
				throw new WebApplicationException(
						responseGenerator.badUpdateRequestUrl(index, entry.getRequest().getUrl()));

			String expectedBaseUrl = serverBase;
			String expectedResourceTypeName = resource.getResourceType().name();
			String expectedId = resource.getIdElement().getIdPart();
			String expectedfullUrl = new IdType(expectedBaseUrl, expectedResourceTypeName, expectedId, null).getValue();

			if (!expectedfullUrl.equals(entry.getFullUrl()))
				throw new WebApplicationException(responseGenerator.badBundleEntryFullUrl(index, entry.getFullUrl()));
			else if (!expectedResourceTypeName.equals(componentes.getPathSegments().get(0))
					|| !expectedId.equals(componentes.getPathSegments().get(1)))
				throw new WebApplicationException(
						responseGenerator.badUpdateRequestUrl(index, entry.getRequest().getUrl()));
		}
		else if (componentes.getPathSegments().size() == 1 && !componentes.getQueryParams().isEmpty())
		{
			if (!entry.getFullUrl().startsWith(URL_UUID_PREFIX))
				throw new WebApplicationException(
						responseGenerator.badUpdateRequestUrl(index, entry.getRequest().getUrl()));
			else if (resource.hasIdElement() && !resource.getIdElement().getValue().startsWith(URL_UUID_PREFIX))
				throw new WebApplicationException(responseGenerator.bundleEntryBadResourceId(index,
						resource.getResourceType().name(), URL_UUID_PREFIX));
			else if (resource.hasIdElement() && !entry.getFullUrl().equals(resource.getIdElement().getValue()))
				throw new WebApplicationException(responseGenerator.badBundleEntryFullUrlVsResourceId(index,
						entry.getFullUrl(), resource.getIdElement().getValue()));
		}
		else
			throw new WebApplicationException(
					responseGenerator.badUpdateRequestUrl(index, entry.getRequest().getUrl()));
	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException
	{
		UriComponents componentes = UriComponentsBuilder.fromUriString(entry.getRequest().getUrl()).build();

		if (componentes.getPathSegments().size() == 2 && componentes.getQueryParams().isEmpty())
			updateById(idTranslationTable, connection, componentes.getPathSegments().get(0),
					componentes.getPathSegments().get(1));
		else if (componentes.getPathSegments().size() == 1 && !componentes.getQueryParams().isEmpty())
			updateByCondition(idTranslationTable, connection, componentes.getPathSegments().get(0),
					parameterConverter.urlDecodeQueryParameters(componentes.getQueryParams()));
		else
			throw new WebApplicationException(
					responseGenerator.badUpdateRequestUrl(index, entry.getRequest().getUrl()));
	}

	private void updateById(Map<String, IdType> idTranslationTable, Connection connection, String resourceTypeName,
			String pathId) throws SQLException
	{
		IdType resourceId = resource.getIdElement();

		if (!Objects.equals(pathId, resourceId.getIdPart()))
			throw new WebApplicationException(
					responseGenerator.pathVsElementIdInBundle(index, resourceTypeName, pathId, resourceId));
		if (resourceId.getBaseUrl() != null && !serverBase.equals(resourceId.getBaseUrl()))
			throw new WebApplicationException(
					responseGenerator.invalidBaseUrlInBundle(index, resourceTypeName, resourceId));

		if (!Objects.equals(resourceTypeName, resource.getResourceType().name()))
			throw new WebApplicationException(responseGenerator.nonMatchingResourceTypeAndRequestUrlInBundle(index,
					resourceTypeName, entry.getRequest().getUrl()));

		Optional<Long> ifMatch = Optional.ofNullable(entry.getRequest().getIfMatch())
				.flatMap(parameterConverter::toEntityTag).flatMap(parameterConverter::toVersion);

		updatedResource = exceptionHandler.handleSqlExAndResourceNotFoundExForUpdateAsCreateAndResouceVersionNonMatchEx(
				resourceTypeName, () -> dao.updateWithTransaction(connection, resource, ifMatch.orElse(null)));
	}

	private void updateByCondition(Map<String, IdType> idTranslationTable, Connection connection,
			String resourceTypeName, Map<String, List<String>> queryParameters) throws SQLException
	{
		if (Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(queryParameters::containsKey))
		{
			logger.warn(
					"Query contains parameter not applicable in this conditional update context: '{}', parameters {} will be ignored",
					UriComponentsBuilder.newInstance()
							.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
					Arrays.toString(SearchQuery.STANDARD_PARAMETERS));

			queryParameters = queryParameters.entrySet().stream()
					.filter(e -> !Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(p -> p.equals(e.getKey())))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		}

		SearchQuery<R> query = dao.createSearchQuery(1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedParams = query.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedParams.isEmpty())
			throw new WebApplicationException(responseGenerator.unsupportedConditionalUpdateQuery(index,
					entry.getRequest().getUrl(), unsupportedParams));

		PartialResult<R> result = exceptionHandler
				.handleSqlException(() -> dao.searchWithTransaction(connection, query));

		// No matches and no id provided or temp id: The server creates the resource.
		if (result.getOverallCount() <= 0
				&& (!resource.hasId() || resource.getIdElement().getValue().startsWith(URL_UUID_PREFIX)))
		{
			id = UUID.randomUUID();
			idTranslationTable.put(entry.getFullUrl(),
					new IdType(resource.getResourceType().toString(), id.toString()));
			updatedResource = dao.createWithTransactionAndId(connection, resource, id);
		}

		// No matches, id provided: The server treats the interaction as an Update as Create interaction (or rejects it,
		// if it does not support Update as Create) -> reject
		else if (result.getOverallCount() <= 0 && resource.hasId())
			// TODO bundle specific error
			throw new WebApplicationException(
					responseGenerator.updateAsCreateNotAllowed(resourceTypeName, resource.getId()));

		// One Match, no resource id provided OR (resource id provided and it matches the found resource):
		// The server performs the update against the matching resource
		else if (result.getOverallCount() == 1)
		{
			R dbResource = result.getPartialResult().get(0);
			IdType dbResourceId = dbResource.getIdElement();

			// BaseUrl - The server base URL (e.g. "http://example.com/fhir")
			// ResourceType - The resource type (e.g. "Patient")
			// IdPart - The ID (e.g. "123")
			// Version - The version ID ("e.g. "456")
			if (!resource.hasId() || resource.getIdElement().getValue().startsWith(URL_UUID_PREFIX))
			{
				resource.setIdElement(dbResourceId);
				idTranslationTable.put(entry.getFullUrl(),
						new IdType(resource.getResourceType().toString(), dbResource.getIdElement().getIdPart()));
				updateById(idTranslationTable, connection, resourceTypeName, resource.getIdElement().getIdPart());
			}
			else if (resource.hasId()
					&& (!resource.getIdElement().hasBaseUrl()
							|| serverBase.equals(resource.getIdElement().getBaseUrl()))
					&& (!resource.getIdElement().hasResourceType()
							|| resourceTypeName.equals(resource.getIdElement().getResourceType()))
					&& (dbResourceId.getIdPart().equals(resource.getIdElement().getIdPart())))
				updateById(idTranslationTable, connection, resourceTypeName, resource.getIdElement().getIdPart());
			else
				// TODO bundle specific error
				throw new WebApplicationException(responseGenerator.badRequestIdsNotMatching(
						dbResourceId.withServerBase(serverBase, resourceTypeName),
						resource.getIdElement().hasBaseUrl() && resource.getIdElement().hasResourceType()
								? resource.getIdElement()
								: resource.getIdElement().withServerBase(serverBase, resourceTypeName)));
		}
		// Multiple matches: The server returns a 412 Precondition Failed error indicating the client's criteria were
		// not selective enough preferably with an OperationOutcome
		else // if (result.getOverallCount() > 1)
			throw new WebApplicationException(responseGenerator.multipleExists(resourceTypeName, UriComponentsBuilder
					.newInstance().replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString()));
	}

	@Override
	public BundleEntryComponent postExecute(Connection connection)
	{
		try
		{
			// retrieving the latest resource from db to include updated references
			Resource updatedResourceWithResolvedReferences = latestOrErrorIfDeletedOrNotFound(connection,
					updatedResource);
			eventManager.handleEvent(eventGenerator.newResourceUpdatedEvent(updatedResourceWithResolvedReferences));
		}
		catch (Exception e)
		{
			logger.warn("Error while handling resource updated event", e);
		}

		BundleEntryComponent resultEntry = new BundleEntryComponent();
		resultEntry.setFullUrl(new IdType(serverBase, updatedResource.getResourceType().name(),
				updatedResource.getIdElement().getIdPart(), null).getValue());
		BundleEntryResponseComponent response = resultEntry.getResponse();
		response.setStatus(Status.OK.getStatusCode() + " " + Status.OK.getReasonPhrase());
		response.setLocation(updatedResource.getIdElement()
				.withServerBase(serverBase, updatedResource.getResourceType().name()).getValue());
		response.setEtag(new EntityTag(updatedResource.getMeta().getVersionId(), true).toString());
		response.setLastModified(updatedResource.getMeta().getLastUpdated());

		return resultEntry;
	}

	private R latestOrErrorIfDeletedOrNotFound(Connection connection, Resource resource) throws Exception
	{
		return dao
				.readWithTransaction(connection,
						parameterConverter.toUuid(resource.getResourceType().name(),
								resource.getIdElement().getIdPart()))
				.orElseThrow(() -> new ResourceNotFoundException(resource.getIdElement().getIdPart()));
	}
}
