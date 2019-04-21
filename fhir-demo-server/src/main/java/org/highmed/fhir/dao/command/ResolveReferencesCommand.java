package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.DomainResourceDao;
import org.highmed.fhir.dao.exception.ResourceDeletedException;
import org.highmed.fhir.dao.exception.ResourceNotFoundException;
import org.highmed.fhir.dao.provider.DaoProvider;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.search.SearchQueryParameterError;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;

public class ResolveReferencesCommand<R extends DomainResource, D extends DomainResourceDao<R>>
		extends AbstractCommand<R, D> implements Command
{
	private static final Logger logger = LoggerFactory.getLogger(ResolveReferencesCommand.class);

	private final ReferenceExtractor referenceExtractor;
	private final ResponseGenerator responseGenerator;
	private final DaoProvider daoProvider;
	private final ExceptionHandler exceptionHandler;

	public ResolveReferencesCommand(int index, Bundle bundle, BundleEntryComponent entry, R resource, String serverBase,
			D dao, ReferenceExtractor referenceExtractor, ResponseGenerator responseGenerator, DaoProvider daoProvider,
			ExceptionHandler exceptionHandler)
	{
		super(5, index, bundle, entry, resource, serverBase, dao);

		this.referenceExtractor = referenceExtractor;
		this.responseGenerator = responseGenerator;
		this.daoProvider = daoProvider;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public void preExecute(Map<String, IdType> idTranslationTable)
	{
	}

	@Override
	public void execute(Map<String, IdType> idTranslationTable, Connection connection)
			throws SQLException, WebApplicationException
	{
		R latest = latest(idTranslationTable, connection);

		boolean resourceNeedsUpdated = referenceExtractor.getReferences(latest)
				.map(resolveReference(idTranslationTable, connection)).anyMatch(b -> b);
		if (resourceNeedsUpdated)
		{
			try
			{
				dao.updateSameRowWithTransaction(connection, latest);
			}
			catch (ResourceNotFoundException e)
			{
				throw exceptionHandler.internalServerError(e);
			}
		}
	}

	private R latest(Map<String, IdType> idTranslationTable, Connection connection) throws SQLException
	{
		try
		{
			String id = idTranslationTable.get(entry.getFullUrl()).getIdPart();
			return dao.readWithTransaction(connection, toUuid(resource.getResourceType().name(), id))
					.orElseThrow(() -> exceptionHandler.internalServerError(new ResourceNotFoundException(id)));
		}
		catch (ResourceDeletedException e)
		{
			throw exceptionHandler.internalServerError(e);
		}
	}

	public UUID toUuid(String resourceTypeName, String id)
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
			throw exceptionHandler.notFound(resourceTypeName, e);
		}
	}

	private Function<ResourceReference, Boolean> resolveReference(Map<String, IdType> idTranslationTable,
			Connection connection) throws WebApplicationException
	{
		return resourceReference ->
		{
			switch (resourceReference.getType(serverBase))
			{
				case TEMPORARY:
					return resolveTemporaryReference(resourceReference, idTranslationTable);
				case LITERAL_INTERNAL:
					return resolveLiteralInternalReference(resourceReference, connection);
				case LITERAL_EXTERNAL:
					return resolveLiteralExternalReference(resourceReference);
				case CONDITIONAL:
					return resolveConditionalReference(resourceReference, connection);
				case LOGICAL:
					return resolveLogicalReference(resourceReference, connection);
				case UNKNOWN:
				default:
					throw new WebApplicationException(
							responseGenerator.unknownReference(index, resource, resourceReference));
			}
		};
	}

	private boolean resolveTemporaryReference(ResourceReference resourceReference,
			Map<String, IdType> idTranslationTable)
	{
		IdType newId = idTranslationTable.get(resourceReference.getReference().getReference());
		if (newId == null)
			throw new WebApplicationException(responseGenerator.unknownReference(index, resource, resourceReference));
		else
			resourceReference.getReference().setReferenceElement(newId);

		return true; // throws exception if reference could not be resolved
	}

	private boolean resolveLiteralInternalReference(ResourceReference resourceReference, Connection connection)
	{
		IdType id = new IdType(resourceReference.getReference().getReference());
		Optional<DomainResourceDao<?>> referenceDao = daoProvider.getDao(id.getResourceType());

		if (referenceDao.isEmpty())
			throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByImplementation(index,
					resource, resourceReference));
		else
		{
			DomainResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
				throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByResource(index,
						resource, resourceReference));

			Optional<IdType> localId = exceptionHandler.handleSqlException(
					() -> d.existsWithTransaction(connection, id.getIdPart(), id.getVersionIdPart()));
			if (localId.isEmpty())
				throw new WebApplicationException(
						responseGenerator.referenceTargetNotFoundLocally(index, resource, resourceReference));
		}

		return false; // throws exception if reference could not be resolved
	}

	private boolean resolveLiteralExternalReference(ResourceReference resourceReference)
	{
		// TODO implement

		logger.warn(
				"Not resolving literal external reference {} of reference at {} in resource of type {} with id {} at bundle index {}",
				resourceReference.getReference().getReference(), resourceReference.getReferenceLocation(),
				resource.getResourceType().name(), resource.getId(), index);

		return false; // throws exception if reference could not be resolvedF
	}

	private boolean resolveConditionalReference(ResourceReference resourceReference, Connection connection)
	{
		UriComponents condition = UriComponentsBuilder.fromUriString(resourceReference.getReference().getReference())
				.build();
		String path = condition.getPath();
		if (path == null || path.isBlank())
			throw new WebApplicationException(
					responseGenerator.referenceTargetBadCondition(index, resource, resourceReference)); // TODO no
																										// target type

		Optional<DomainResourceDao<?>> referenceDao = daoProvider.getDao(path);

		if (referenceDao.isEmpty())
			throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByImplementation(index,
					resource, resourceReference));
		else
		{
			DomainResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
				throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByResource(index,
						resource, resourceReference));

			DomainResource target = search(connection, d, resourceReference, condition.getQueryParams(), true);

			resourceReference.getReference().setIdentifier(null).setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		return true; // throws exception if reference could not be resolved
	}

	private boolean resolveLogicalReference(ResourceReference resourceReference, Connection connection)
	{
		String targetType = resourceReference.getReference().getType();

		Optional<DomainResourceDao<?>> referenceDao = daoProvider.getDao(targetType);

		if (referenceDao.isEmpty())
			throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByImplementation(index,
					resource, resourceReference));
		else
		{
			DomainResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
				throw new WebApplicationException(responseGenerator.referenceTargetTypeNotSupportedByResource(index,
						resource, resourceReference));

			Identifier targetIdentifier = resourceReference.getReference().getIdentifier();
			DomainResource target = search(connection, d, resourceReference, Map.of("identifier",
					Collections.singletonList(targetIdentifier.getSystem() + "|" + targetIdentifier.getValue())), true);

			resourceReference.getReference().setIdentifier(null).setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		return true; // throws exception if reference could not be resolved
	}

	private DomainResource search(Connection connection, DomainResourceDao<?> referenceTargetDao,
			ResourceReference resourceReference, Map<String, List<String>> queryParameters,
			boolean logicalNoConditional)
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

		SearchQuery<?> query = referenceTargetDao.createSearchQuery(1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
			throw new WebApplicationException(
					responseGenerator.badReference(logicalNoConditional, index, resource, resourceReference,
							UriComponentsBuilder.newInstance()
									.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
							unsupportedQueryParameters));

		PartialResult<?> result = exceptionHandler
				.handleSqlException(() -> referenceTargetDao.searchWithTransaction(connection, query));

		if (result.getOverallCount() <= 0)
		{
			if (logicalNoConditional)
				throw new WebApplicationException(responseGenerator.referenceTargetNotFoundLocallyByIdentifier(index,
						resource, resourceReference));
			else
				throw new WebApplicationException(responseGenerator.referenceTargetNotFoundLocallyByCondition(index,
						resource, resourceReference, UriComponentsBuilder.newInstance()
								.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString()));
		}
		else if (result.getOverallCount() == 1)
		{
			return result.getPartialResult().get(0);
		}
		else // if (result.getOverallCount() > 1)
		{
			if (logicalNoConditional)
				throw new WebApplicationException(responseGenerator.referenceTargetMultipleMatchesLocallyByIdentifier(
						index, resource, resourceReference, result.getOverallCount()));
			else
				throw new WebApplicationException(responseGenerator.referenceTargetMultipleMatchesLocallyByCondition(
						index, resource, resourceReference, result.getOverallCount(), UriComponentsBuilder.newInstance()
								.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString()));
		}
	}

	@Override
	public BundleEntryComponent postExecute()
	{
		return null;
	}
}
