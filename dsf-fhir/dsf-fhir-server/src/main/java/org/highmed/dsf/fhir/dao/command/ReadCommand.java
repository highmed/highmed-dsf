package org.highmed.dsf.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.prefer.PreferHandlingType;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.validation.SnapshotGenerator;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.rest.api.Constants;

public class ReadCommand extends AbstractCommand implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(ReadCommand.class);

	private final int defaultPageCount;

	private final DaoProvider daoProvider;
	private final ParameterConverter parameterConverter;
	private final ResponseGenerator responseGenerator;
	private final ExceptionHandler exceptionHandler;
	private final ReferenceCleaner referenceCleaner;
	private final PreferHandlingType handlingType;

	private Bundle multipleResult;
	private Resource singleResult;
	private OperationOutcome singleResultSearchWarning;
	private Response responseResult;

	public ReadCommand(int index, User user, PreferReturnType returnType, Bundle bundle, BundleEntryComponent entry,
			String serverBase, AuthorizationHelper authorizationHelper, int defaultPageCount, DaoProvider daoProvider,
			ParameterConverter parameterConverter, ResponseGenerator responseGenerator,
			ExceptionHandler exceptionHandler, ReferenceCleaner referenceCleaner, PreferHandlingType handlingType)
	{
		super(5, index, user, returnType, bundle, entry, serverBase, authorizationHelper);

		this.defaultPageCount = defaultPageCount;

		this.daoProvider = daoProvider;
		this.parameterConverter = parameterConverter;
		this.responseGenerator = responseGenerator;
		this.exceptionHandler = exceptionHandler;
		this.referenceCleaner = referenceCleaner;
		this.handlingType = handlingType;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable, Connection connection,
			ValidationHelper validationHelper, SnapshotGenerator snapshotGenerator)
	{
	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection,
			ValidationHelper validationHelper, SnapshotGenerator snapshotGenerator)
			throws SQLException, WebApplicationException
	{
		String requestUrl = entry.getRequest().getUrl();

		logger.debug("Executing request for url {}", requestUrl);

		if (requestUrl.startsWith(URL_UUID_PREFIX))
			requestUrl = idTranslationTable.getOrDefault(requestUrl, new IdType(requestUrl)).getValue();

		UriComponents componentes = UriComponentsBuilder.fromUriString(requestUrl).build();

		if (componentes.getPathSegments().size() == 2 && componentes.getQueryParams().isEmpty())
			readById(connection, componentes.getPathSegments().get(0), componentes.getPathSegments().get(1));
		else if (componentes.getPathSegments().size() == 4
				&& Constants.PARAM_HISTORY.equals(componentes.getPathSegments().get(2))
				&& componentes.getQueryParams().isEmpty())
			readByIdAndVersion(connection, componentes.getPathSegments().get(0), componentes.getPathSegments().get(1),
					componentes.getPathSegments().get(3));
		else if (componentes.getPathSegments().size() == 1 && !componentes.getQueryParams().isEmpty())
			readByCondition(connection, componentes.getPathSegments().get(0),
					parameterConverter.urlDecodeQueryParameters(componentes.getQueryParams()));
		else
			throw new WebApplicationException(responseGenerator.badReadRequestUrl(index, requestUrl));
	}

	private void readById(Connection connection, String resourceTypeName, String id)
	{
		Optional<ResourceDao<? extends Resource>> optDao = daoProvider.getDao(resourceTypeName);
		if (optDao.isEmpty())
			responseResult = Response.status(Status.NOT_FOUND).build();
		else
		{
			ResourceDao<? extends Resource> dao = optDao.get();
			Optional<?> read = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
					() -> dao.readWithTransaction(connection, parameterConverter.toUuid(resourceTypeName, id)));

			if (read.isEmpty())
				responseResult = Response.status(Status.NOT_FOUND).build();
			else
			{
				Resource r = (Resource) read.get();

				Optional<Date> ifModifiedSince = Optional.ofNullable(entry.getRequest().getIfModifiedSince());
				Optional<EntityTag> ifNoneMatch = Optional.ofNullable(entry.getRequest().getIfNoneMatch())
						.flatMap(parameterConverter::toEntityTag);

				EntityTag resourceTag = new EntityTag(r.getMeta().getVersionId(), true);
				if (ifNoneMatch.map(t -> t.equals(resourceTag)).orElse(false)
						|| ifModifiedSince.map(d -> r.getMeta().getLastUpdated().after(d)).orElse(false))
					responseResult = Response.notModified(resourceTag).lastModified(r.getMeta().getLastUpdated())
							.build();
				else
					singleResult = r;

				authorizationHelper.checkReadAllowed(connection, user, r);
			}
		}
	}

	private void readByIdAndVersion(Connection connection, String resourceTypeName, String id, String version)
	{
		Optional<ResourceDao<? extends Resource>> optDao = daoProvider.getDao(resourceTypeName);
		Optional<Long> longVersion = parameterConverter.toVersion(version);
		if (optDao.isEmpty() || longVersion.isEmpty())
			responseResult = Response.status(Status.NOT_FOUND).build();
		else
		{
			ResourceDao<? extends Resource> dao = optDao.get();
			Optional<?> read = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
					() -> dao.readVersionWithTransaction(connection, parameterConverter.toUuid(resourceTypeName, id),
							longVersion.get()));
			if (read.isEmpty())
				responseResult = Response.status(Status.NOT_FOUND).build();
			else
			{
				Resource r = (Resource) read.get();

				Optional<Date> ifModifiedSince = Optional.ofNullable(entry.getRequest().getIfModifiedSince());
				Optional<EntityTag> ifNoneMatch = Optional.ofNullable(entry.getRequest().getIfNoneMatch())
						.flatMap(parameterConverter::toEntityTag);

				EntityTag resourceTag = new EntityTag(r.getMeta().getVersionId(), true);
				if (ifNoneMatch.map(t -> t.equals(resourceTag)).orElse(false)
						|| ifModifiedSince.map(d -> r.getMeta().getLastUpdated().after(d)).orElse(false))
					responseResult = Response.notModified(resourceTag).lastModified(r.getMeta().getLastUpdated())
							.build();
				else
					singleResult = r;

				authorizationHelper.checkReadAllowed(connection, user, r);
			}
		}
	}

	private void readByCondition(Connection connection, String resourceTypeName,
			Map<String, List<String>> cleanQueryParameters)
	{
		Optional<ResourceDao<? extends Resource>> optDao = daoProvider.getDao(resourceTypeName);
		if (optDao.isEmpty())
			responseResult = Response.status(Status.NOT_FOUND).build();
		else
		{
			Integer page = parameterConverter.getFirstInt(cleanQueryParameters, SearchQuery.PARAMETER_PAGE);
			int effectivePage = page == null ? 1 : page;

			Integer count = parameterConverter.getFirstInt(cleanQueryParameters, SearchQuery.PARAMETER_COUNT);
			int effectiveCount = (count == null || count < 0) ? defaultPageCount : count;

			SearchQuery<? extends Resource> query = optDao.get().createSearchQuery(user, effectivePage, effectiveCount);
			query.configureParameters(cleanQueryParameters);
			List<SearchQueryParameterError> errors = query.getUnsupportedQueryParameters(cleanQueryParameters);

			if (!errors.isEmpty() && PreferHandlingType.STRICT.equals(handlingType))
				throw new WebApplicationException(responseGenerator.response(Status.BAD_REQUEST,
						responseGenerator.toOperationOutcomeError(errors), MediaType.APPLICATION_XML_TYPE).build());

			PartialResult<? extends Resource> result = exceptionHandler
					.handleSqlException(() -> optDao.get().searchWithTransaction(connection, query));

			UriBuilder bundleUri = query.configureBundleUri(UriBuilder.fromPath(serverBase + "/" + resourceTypeName));

			multipleResult = responseGenerator.createSearchSet(result, errors, bundleUri, null, null);

			// map single search result from multipleResult field to singleResult field
			if (multipleResult != null && multipleResult.getEntry().size() == 1)
			{
				singleResult = (Resource) multipleResult.getEntry().get(0).getResource();
				multipleResult = null;

				authorizationHelper.checkReadAllowed(connection, user, singleResult);
			}
			else if (multipleResult != null && multipleResult.getEntry().size() == 2
					&& SearchEntryMode.MATCH.equals(multipleResult.getEntry().get(0).getSearch().getMode())
					&& SearchEntryMode.OUTCOME.equals(multipleResult.getEntry().get(1).getSearch().getMode()))
			{
				singleResult = (Resource) multipleResult.getEntry().get(0).getResource();
				singleResultSearchWarning = (OperationOutcome) multipleResult.getEntry().get(1).getResource();
				multipleResult = null;

				authorizationHelper.checkReadAllowed(connection, user, singleResult);
			}
			else if (multipleResult != null && multipleResult.getEntry().size() == 2
					&& SearchEntryMode.MATCH.equals(multipleResult.getEntry().get(1).getSearch().getMode())
					&& SearchEntryMode.OUTCOME.equals(multipleResult.getEntry().get(0).getSearch().getMode()))
			{
				singleResult = (Resource) multipleResult.getEntry().get(1).getResource();
				singleResultSearchWarning = (OperationOutcome) multipleResult.getEntry().get(0).getResource();
				multipleResult = null;

				authorizationHelper.checkReadAllowed(connection, user, singleResult);
			}
			else
			{
				authorizationHelper.checkSearchAllowed(user, resourceTypeName);
				authorizationHelper.filterIncludeResults(connection, user, multipleResult);
			}
		}
	}

	@Override
	public Optional<BundleEntryComponent> postExecute(Connection connection, EventHandler eventHandler)
	{
		if (singleResult != null)
		{
			referenceCleaner.cleanLiteralReferences(singleResult);

			BundleEntryComponent resultEntry = new BundleEntryComponent();
			resultEntry.setFullUrl(new IdType(serverBase, singleResult.getResourceType().name(),
					singleResult.getIdElement().getIdPart(), null).getValue());
			BundleEntryResponseComponent response = resultEntry.getResponse();
			response.setStatus(Status.OK.getStatusCode() + " " + Status.OK.getReasonPhrase());
			response.setLocation(singleResult.getIdElement()
					.withServerBase(serverBase, singleResult.getResourceType().name()).getValue());
			response.setEtag(new EntityTag(singleResult.getMeta().getVersionId(), true).toString());
			response.setLastModified(singleResult.getMeta().getLastUpdated());

			setSingleResult(resultEntry, singleResult);

			if (singleResultSearchWarning != null)
				response.setOutcome(singleResultSearchWarning);

			return Optional.of(resultEntry);
		}
		else if (multipleResult != null)
		{
			// clean literal references from bundle entries
			multipleResult.getEntry().stream().filter(BundleEntryComponent::hasResource)
					.map(BundleEntryComponent::getResource).forEach(referenceCleaner::cleanLiteralReferences);

			BundleEntryComponent resultEntry = new BundleEntryComponent();
			resultEntry.setFullUrl(URL_UUID_PREFIX + UUID.randomUUID().toString());
			BundleEntryResponseComponent response = resultEntry.getResponse();
			response.setStatus(Status.OK.getStatusCode() + " " + Status.OK.getReasonPhrase());

			setMultipleResult(resultEntry, multipleResult);

			return Optional.of(resultEntry);
		}
		else
		{
			BundleEntryComponent resultEntry = new BundleEntryComponent();
			BundleEntryResponseComponent response = resultEntry.getResponse();
			response.setStatus(responseResult.getStatusInfo().getStatusCode() + " "
					+ responseResult.getStatusInfo().getReasonPhrase());

			if (responseResult.getEntityTag() != null)
				response.setEtag(responseResult.getEntityTag().getValue());
			if (responseResult.getLastModified() != null)
				response.setLastModified(responseResult.getLastModified());

			return Optional.of(resultEntry);
		}
	}

	protected void setMultipleResult(BundleEntryComponent resultEntry, Bundle multipleResult)
	{
		resultEntry.setResource(multipleResult);
	}

	protected void setSingleResult(BundleEntryComponent resultEntry, Resource singleResult)
	{
		resultEntry.setResource(singleResult);
	}
}
