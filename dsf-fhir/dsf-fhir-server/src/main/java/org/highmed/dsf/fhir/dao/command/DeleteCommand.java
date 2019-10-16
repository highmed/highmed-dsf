package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
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

public class DeleteCommand extends AbstractCommand implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(DeleteCommand.class);

	private final ResponseGenerator responseGenerator;
	private final DaoProvider daoProvider;
	private final ExceptionHandler exceptionHandler;
	private final ParameterConverter parameterConverter;
	private final EventManager eventManager;
	private final EventGenerator eventGenerator;

	private boolean deleted;
	private String resourceTypeName;
	private Class<? extends Resource> resourceType;
	private String id;

	public DeleteCommand(int index, Bundle bundle, BundleEntryComponent entry, String serverBase,
			ResponseGenerator responseGenerator, DaoProvider daoProvider, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, EventManager eventManager, EventGenerator eventGenerator)
	{
		super(1, index, bundle, entry, serverBase);

		this.responseGenerator = responseGenerator;
		this.daoProvider = daoProvider;
		this.exceptionHandler = exceptionHandler;
		this.parameterConverter = parameterConverter;
		this.eventManager = eventManager;
		this.eventGenerator = eventGenerator;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable)
	{
	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException
	{
		UriComponents componentes = UriComponentsBuilder.fromUriString(entry.getRequest().getUrl()).build();

		if (componentes.getPathSegments().size() == 2 && componentes.getQueryParams().isEmpty())
			deleteById(connection, componentes.getPathSegments().get(0), componentes.getPathSegments().get(1));
		else if (componentes.getPathSegments().size() == 1 && !componentes.getQueryParams().isEmpty())
			deleteByCondition(connection, componentes.getPathSegments().get(0),
					parameterConverter.urlDecodeQueryParameters(componentes.getQueryParams()));
		else
			throw new WebApplicationException(
					responseGenerator.badDeleteRequestUrl(index, entry.getRequest().getUrl()));

		resourceTypeName = componentes.getPathSegments().get(0);
	}

	private void deleteById(Connection connection, String resourceTypeName, String id)
	{
		Optional<ResourceDao<?>> dao = daoProvider.getDao(resourceTypeName);

		if (dao.isEmpty())
			throw new WebApplicationException(
					responseGenerator.resourceTypeNotSupportedByImplementation(index, resourceTypeName));
		else
		{
			deleted = exceptionHandler.handleSqlAndResourceNotFoundException(resourceTypeName,
					() -> dao.get().deleteWithTransaction(connection, parameterConverter.toUuid(resourceTypeName, id)));

			this.resourceType = dao.get().getResourceType();
			this.id = id;
		}
	}

	private void deleteByCondition(Connection connection, String resourceTypeName,
			Map<String, List<String>> queryParameters)
	{
		Optional<ResourceDao<?>> dao = daoProvider.getDao(resourceTypeName);

		if (dao.isEmpty())
			throw new WebApplicationException(
					responseGenerator.resourceTypeNotSupportedByImplementation(index, resourceTypeName));
		else
		{
			Optional<Resource> resourceToDelete = search(connection, dao.get(), queryParameters);
			if (resourceToDelete.isPresent())
			{
				deleted = exceptionHandler.handleSqlAndResourceNotFoundException(resourceTypeName,
						() -> dao.get().deleteWithTransaction(connection, parameterConverter.toUuid(resourceTypeName,
								resourceToDelete.get().getIdElement().getIdPart())));

				this.resourceType = dao.get().getResourceType();
				this.id = resourceToDelete.get().getIdElement().getIdPart();
			}
		}
	}

	private Optional<Resource> search(Connection connection, ResourceDao<?> dao,
			Map<String, List<String>> queryParameters)
	{
		if (Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(queryParameters::containsKey))
		{
			logger.warn(
					"Query contains parameter not applicable in this conditional delete context: '{}', parameters {} will be ignored",
					UriComponentsBuilder.newInstance()
							.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
					Arrays.toString(SearchQuery.STANDARD_PARAMETERS));

			queryParameters = queryParameters.entrySet().stream()
					.filter(e -> !Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(p -> p.equals(e.getKey())))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		}

		SearchQuery<?> query = dao.createSearchQuery(1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
			throw new WebApplicationException(responseGenerator.badConditionalDeleteRequest(index,
					UriComponentsBuilder.newInstance()
							.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
					unsupportedQueryParameters));

		PartialResult<?> result = exceptionHandler
				.handleSqlException(() -> dao.searchWithTransaction(connection, query));

		if (result.getOverallCount() <= 0)
		{
			return Optional.empty();
		}
		else if (result.getOverallCount() == 1)
		{
			return Optional.of(result.getPartialResult().get(0));
		}
		else // if (result.getOverallCount() > 1)
		{
			throw new WebApplicationException(responseGenerator.badConditionalDeleteRequestMultipleMatches(index,
					resourceTypeName, UriComponentsBuilder.newInstance()
							.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString()));
		}
	}

	@Override
	public BundleEntryComponent postExecute(Connection connection)
	{
		try
		{
			if (deleted)
				eventManager.handleEvent(eventGenerator.newResourceDeletedEvent(resourceType, id));
		}
		catch (Exception e)
		{
			logger.warn("Error while handling resource deleted event", e);
		}

		BundleEntryComponent resultEntry = new BundleEntryComponent();
		BundleEntryResponseComponent response = resultEntry.getResponse();
		if (resourceTypeName != null && id != null)
		{
			response.setStatus(Status.OK.getStatusCode() + " " + Status.OK.getReasonPhrase());
			response.setOutcome(responseGenerator.resourceDeleted(resourceTypeName, id));
		}
		else
			response.setStatus(Status.NO_CONTENT.getStatusCode() + " " + Status.NO_CONTENT.getReasonPhrase());

		return resultEntry;
	}
}
