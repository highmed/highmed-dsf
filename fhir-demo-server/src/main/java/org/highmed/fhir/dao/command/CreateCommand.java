package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response.Status;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.search.SearchQueryParameterError;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.rest.api.Constants;

public class CreateCommand<R extends DomainResource, D extends DomainResourceDao<R>> implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(CreateCommand.class);

	private static final String UUID_PREFIX = "urn:uuid:";

	private final int index;

	protected final String serverBase;
	protected final D dao;
	protected final ReferenceReplacer replacer;
	protected final ResponseGenerator responseGenerator;
	protected final ExceptionHandler exceptionHandler;
	protected final EventManager eventManager;
	protected final EventGenerator eventGenerator;

	protected final Bundle bundle;
	protected final BundleEntryComponent entry;
	protected final R resource;

	protected UUID id;
	protected R createdResource;

	public CreateCommand(int index, String serverBase, D dao, ReferenceReplacer replacer,
			ResponseGenerator responseGenerator, ExceptionHandler exceptionHandler, EventManager eventManager,
			EventGenerator eventGenerator, Bundle bundle, BundleEntryComponent entry, R resource)
	{
		this.index = index;
		this.serverBase = serverBase;
		this.dao = dao;
		this.replacer = replacer;
		this.responseGenerator = responseGenerator;
		this.exceptionHandler = exceptionHandler;
		this.eventManager = eventManager;
		this.eventGenerator = eventGenerator;
		this.bundle = bundle;
		this.entry = entry;
		this.resource = resource;
	}

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public void preExecute(Connection connection) throws SQLException, WebApplicationException
	{
		id = UUID.randomUUID();

		String fullUrl = entry.getFullUrl();
		if (fullUrl.startsWith(UUID_PREFIX))
		{
			bundle.getEntry().stream().map(BundleEntryComponent::getResource).filter(r -> r instanceof DomainResource)
					.map(r -> (DomainResource) r).forEach(r -> replacer.setReference(r, resource.getClass(), fullUrl,
							new IdType(resource.getResourceType().toString(), id.toString()).getValue()));
		}
	}

	@Override
	public void execute(Connection connection) throws SQLException, WebApplicationException
	{
		checkAlreadyExists(entry.getRequest().getIfNoneExist(), resource.getResourceType());

		createdResource = dao.createWithTransactionAndId(connection, resource, id);
	}

	private void checkAlreadyExists(String ifNoneExist, ResourceType resourceType) throws WebApplicationException
	{
		if (ifNoneExist == null)
			return; // header not found, nothing to check against

		if (ifNoneExist.isBlank())
			throw new WebApplicationException(responseGenerator.badIfNoneExistHeaderValue(ifNoneExist));

		if (!ifNoneExist.contains("?"))
			ifNoneExist = '?' + ifNoneExist;

		UriComponents componentes = UriComponentsBuilder.fromUriString(ifNoneExist).build();
		String path = componentes.getPath();
		if (path != null && !path.isBlank())
			throw new WebApplicationException(responseGenerator.badIfNoneExistHeaderValue(ifNoneExist));

		Map<String, List<String>> queryParameters = componentes.getQueryParams();
		if (Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(queryParameters::containsKey))
		{
			logger.warn(
					"{} Header contains query parameter not applicable in this conditional create context: '{}', parameters {} will be ignored",
					Constants.HEADER_IF_NONE_EXIST, ifNoneExist, Arrays.toString(SearchQuery.STANDARD_PARAMETERS));

			queryParameters = queryParameters.entrySet().stream()
					.filter(e -> !Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(p -> p.equals(e.getKey())))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		}

		SearchQuery<R> query = dao.createSearchQuery(0, 0);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
			throw new WebApplicationException(
					responseGenerator.badIfNoneExistHeaderValue(ifNoneExist, unsupportedQueryParameters));

		PartialResult<R> result = exceptionHandler.handleSqlException(() -> dao.search(query));
		if (result.getOverallCount() == 1)
			throw new WebApplicationException(responseGenerator.oneExists(resourceType.name(), ifNoneExist));
		else if (result.getOverallCount() > 1)
			throw new WebApplicationException(responseGenerator.multipleExists(resourceType.name(), ifNoneExist));
	}

	@Override
	public BundleEntryComponent postExecute(Connection connection) throws SQLException, WebApplicationException
	{
		eventManager.handleEvent(eventGenerator.newResourceCreatedEvent(createdResource));

		BundleEntryComponent resultEntry = new BundleEntryComponent();
		resultEntry.setFullUrl(new IdType(serverBase, createdResource.getResourceType().name(),
				createdResource.getIdElement().getIdPart(), null).getValue());
		BundleEntryResponseComponent response = resultEntry.getResponse();
		response.setStatus(Status.CREATED.getStatusCode() + " " + Status.CREATED.getReasonPhrase());
		response.setLocation(createdResource.getIdElement()
				.withServerBase(serverBase, createdResource.getResourceType().name()).getValue());
		response.setEtag(new EntityTag(createdResource.getMeta().getVersionId(), true).toString());
		response.setLastModified(createdResource.getMeta().getLastUpdated());

		return resultEntry;
	}
}
