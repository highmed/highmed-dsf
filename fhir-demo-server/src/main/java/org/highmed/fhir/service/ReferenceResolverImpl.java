package org.highmed.fhir.service;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.highmed.fhir.dao.ResourceDao;
import org.highmed.fhir.dao.command.ResourceReference;
import org.highmed.fhir.dao.command.ResourceReference.ReferenceType;
import org.highmed.fhir.dao.provider.DaoProvider;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.search.PartialResult;
import org.highmed.fhir.search.SearchQuery;
import org.highmed.fhir.search.SearchQueryParameterError;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class ReferenceResolverImpl implements ReferenceResolver, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ReferenceResolverImpl.class);

	private final String serverBase;
	private final DaoProvider daoProvider;
	private final ResponseGenerator responseGenerator;
	private final ExceptionHandler exceptionHandler;

	public ReferenceResolverImpl(String serverBase, DaoProvider daoProvider, ResponseGenerator responseGenerator,
			ExceptionHandler exceptionHandler)
	{
		this.serverBase = serverBase;
		this.daoProvider = daoProvider;
		this.responseGenerator = responseGenerator;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(daoProvider, "daoProvider");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
	}

	@Override
	public boolean resolveReference(DomainResource resource, ResourceReference resourceReference, Connection connection)
	{
		return resolveReference(resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveReference(DomainResource resource, Integer bundleIndex, ResourceReference resourceReference,
			Connection connection)
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(resourceReference, "resourceReference");
		Objects.requireNonNull(connection, "connection");

		switch (resourceReference.getType(serverBase))
		{
			case LITERAL_INTERNAL:
				return resolveLiteralInternalReference(resource, bundleIndex, resourceReference, connection);
			case LITERAL_EXTERNAL:
				return resolveLiteralExternalReference(resource, bundleIndex, resourceReference);
			case CONDITIONAL:
				return resolveConditionalReference(resource, bundleIndex, resourceReference, connection);
			case LOGICAL:
				return resolveLogicalReference(resource, bundleIndex, resourceReference, connection);
			default:
				throw new IllegalArgumentException(
						"Reference of type " + resourceReference.getType(serverBase) + " not supported");
		}
	}

	@Override
	public boolean resolveLiteralInternalReference(DomainResource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException
	{
		return resolveLiteralInternalReference(resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveLiteralInternalReference(DomainResource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(resourceReference, "resourceReference");
		Objects.requireNonNull(connection, "connection");
		if (!ReferenceType.LITERAL_INTERNAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a literal internal reference");

		IdType id = new IdType(resourceReference.getReference().getReference());
		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(id.getResourceType());

		if (referenceDao.isEmpty())
			throw new WebApplicationException(responseGenerator
					.referenceTargetTypeNotSupportedByImplementation(bundleIndex, resource, resourceReference));
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
				throw new WebApplicationException(responseGenerator
						.referenceTargetTypeNotSupportedByResource(bundleIndex, resource, resourceReference));

			Optional<IdType> localId = exceptionHandler.handleSqlException(
					() -> d.existsWithTransaction(connection, id.getIdPart(), id.getVersionIdPart()));
			if (localId.isEmpty())
				throw new WebApplicationException(
						responseGenerator.referenceTargetNotFoundLocally(bundleIndex, resource, resourceReference));
		}

		return false; // throws exception if reference could not be resolved
	}

	@Override
	public boolean resolveLiteralExternalReference(DomainResource resource, ResourceReference resourceReference)
			throws WebApplicationException, IllegalArgumentException
	{
		return resolveLiteralExternalReference(resource, null, resourceReference);
	}

	@Override
	public boolean resolveLiteralExternalReference(DomainResource resource, Integer bundleIndex,
			ResourceReference resourceReference) throws WebApplicationException, IllegalArgumentException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(resourceReference, "resourceReference");
		if (!ReferenceType.LITERAL_EXTERNAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a literal external reference");

		// TODO implement me

		logger.warn(
				"Not resolving literal external reference {} of reference at {} in resource of type {} with id {} at bundle index {}, testing of literal external references not implemented yet",
				resourceReference.getReference().getReference(), resourceReference.getReferenceLocation(),
				resource.getResourceType().name(), resource.getId(), bundleIndex);

		return false; // throws exception if reference could not be resolved
	}

	@Override
	public boolean resolveConditionalReference(DomainResource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException
	{
		return resolveConditionalReference(resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveConditionalReference(DomainResource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(resourceReference, "resourceReference");
		Objects.requireNonNull(connection, "connection");
		if (!ReferenceType.CONDITIONAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a conditional reference");

		UriComponents condition = UriComponentsBuilder.fromUriString(resourceReference.getReference().getReference())
				.build();
		String path = condition.getPath();
		if (path == null || path.isBlank())
			throw new WebApplicationException(
					responseGenerator.referenceTargetBadCondition(bundleIndex, resource, resourceReference));

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(path);

		if (referenceDao.isEmpty())
			throw new WebApplicationException(responseGenerator
					.referenceTargetTypeNotSupportedByImplementation(bundleIndex, resource, resourceReference));
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
				throw new WebApplicationException(responseGenerator
						.referenceTargetTypeNotSupportedByResource(bundleIndex, resource, resourceReference));

			Resource target = search(resource, bundleIndex, connection, d, resourceReference,
					condition.getQueryParams(), true);

			resourceReference.getReference().setIdentifier(null).setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		return true; // throws exception if reference could not be resolved
	}

	@Override
	public boolean resolveLogicalReference(DomainResource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException
	{
		return resolveLogicalReference(resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveLogicalReference(DomainResource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(resourceReference, "resourceReference");
		Objects.requireNonNull(connection, "connection");
		if (!ReferenceType.LOGICAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a logical reference");

		String targetType = resourceReference.getReference().getType();

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(targetType);

		if (referenceDao.isEmpty())
			throw new WebApplicationException(responseGenerator
					.referenceTargetTypeNotSupportedByImplementation(bundleIndex, resource, resourceReference));
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
				throw new WebApplicationException(responseGenerator
						.referenceTargetTypeNotSupportedByResource(bundleIndex, resource, resourceReference));

			Identifier targetIdentifier = resourceReference.getReference().getIdentifier();
			Resource target = search(resource, bundleIndex, connection, d, resourceReference, Map.of("identifier",
					Collections.singletonList(targetIdentifier.getSystem() + "|" + targetIdentifier.getValue())), true);

			resourceReference.getReference().setIdentifier(null).setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		return true; // throws exception if reference could not be resolved
	}

	private Resource search(DomainResource resource, Integer bundleIndex, Connection connection,
			ResourceDao<?> referenceTargetDao, ResourceReference resourceReference,
			Map<String, List<String>> queryParameters, boolean logicalNoConditional)
	{
		if (Arrays.stream(SearchQuery.STANDARD_PARAMETERS).anyMatch(queryParameters::containsKey))
		{
			logger.warn(
					"Query contains parameter not applicable in this resolve reference context: '{}', parameters {} will be ignored",
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
					responseGenerator.badReference(logicalNoConditional, bundleIndex, resource, resourceReference,
							UriComponentsBuilder.newInstance()
									.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
							unsupportedQueryParameters));

		PartialResult<?> result = exceptionHandler
				.handleSqlException(() -> referenceTargetDao.searchWithTransaction(connection, query));

		if (result.getOverallCount() <= 0)
		{
			if (logicalNoConditional)
				throw new WebApplicationException(responseGenerator
						.referenceTargetNotFoundLocallyByIdentifier(bundleIndex, resource, resourceReference));
			else
				throw new WebApplicationException(responseGenerator.referenceTargetNotFoundLocallyByCondition(
						bundleIndex, resource, resourceReference, UriComponentsBuilder.newInstance()
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
						bundleIndex, resource, resourceReference, result.getOverallCount()));
			else
				throw new WebApplicationException(responseGenerator.referenceTargetMultipleMatchesLocallyByCondition(
						bundleIndex, resource, resourceReference, result.getOverallCount(),
						UriComponentsBuilder.newInstance()
								.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString()));
		}
	}
}
