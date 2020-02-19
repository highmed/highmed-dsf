package org.highmed.dsf.fhir.service;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.client.ClientProvider;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
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
	private final ClientProvider clientProvider;
	private final ParameterConverter parameterConverter;

	public ReferenceResolverImpl(String serverBase, DaoProvider daoProvider, ResponseGenerator responseGenerator,
			ExceptionHandler exceptionHandler, ClientProvider clientProvider, ParameterConverter parameterConverter)
	{
		this.serverBase = serverBase;
		this.daoProvider = daoProvider;
		this.responseGenerator = responseGenerator;
		this.exceptionHandler = exceptionHandler;
		this.clientProvider = clientProvider;
		this.parameterConverter = parameterConverter;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(daoProvider, "daoProvider");
		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(clientProvider, "clientProvider");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
	}

	@Override
	public Optional<Resource> resolveReference(User user, String referenceLocation, Reference reference,
			List<Class<? extends Resource>> referenceTypes)
	{
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(reference, "reference");
		
		return resolveReference(user, new ResourceReference(referenceLocation, reference, referenceTypes));
	}

	@Override
	public Optional<Resource> resolveReference(User user, ResourceReference reference)
	{
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(reference, "reference");
		
		switch (reference.getType(serverBase))
		{
			case LITERAL_INTERNAL:
				return resolveLiteralInternalReference(reference);
			case LITERAL_EXTERNAL:
				return resolveLiteralExternalReference(reference);
			case CONDITIONAL:
				return resolveConditionalReference(user, reference);
			case LOGICAL:
				return resolveLogicalReference(user, reference);
			default:
				throw new IllegalArgumentException(
						"Reference of type " + reference.getType(serverBase) + " not supported");
		}
	}

	@Override
	public boolean resolveReference(User user, Resource resource, ResourceReference resourceReference, Connection connection)
	{
		return resolveReference(user, resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveReference(User user, Resource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
	{
		Objects.requireNonNull(user, "user");
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
				return resolveConditionalReference(user, resource, bundleIndex, resourceReference, connection);
			case LOGICAL:
				return resolveLogicalReference(user, resource, bundleIndex, resourceReference, connection);
			default:
				throw new IllegalArgumentException(
						"Reference of type " + resourceReference.getType(serverBase) + " not supported");
		}
	}

	private Optional<Resource> resolveLiteralInternalReference(ResourceReference resourceReference)
	{
		Objects.requireNonNull(resourceReference, "resourceReference");
		if (!ReferenceType.LITERAL_INTERNAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a literal internal reference");

		IdType id = new IdType(resourceReference.getReference().getReference());
		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(id.getResourceType());

		if (referenceDao.isEmpty())
		{
			logger.warn("Reference target type of reference at {} not supported by this implementation",
					resourceReference.getReferenceLocation());
			return Optional.empty();
		}
		else
		{
			@SuppressWarnings("unchecked")
			ResourceDao<Resource> d = (ResourceDao<Resource>) referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
			{
				logger.warn("Reference target type of reference at {} not supported",
						resourceReference.getReferenceLocation());
				return Optional.empty();
			}

			Optional<UUID> uuid = parameterConverter.toUuid(id.getIdPart());

			if (id.hasVersionIdPart())
				return uuid.flatMap(i -> exceptionHandler.catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(
						() -> d.read(i), Optional::empty, Optional::empty));
			else
				return uuid.flatMap(i -> exceptionHandler.catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(
						() -> d.readVersion(i, id.getVersionIdPartAsLong()), Optional::empty, Optional::empty));
		}
	}

	@Override
	public boolean resolveLiteralInternalReference(Resource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException
	{
		return resolveLiteralInternalReference(resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveLiteralInternalReference(Resource resource, Integer bundleIndex,
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

			boolean exists = exceptionHandler.handleSqlException(
					() -> d.existsNotDeletedWithTransaction(connection, id.getIdPart(), id.getVersionIdPart()));
			if (!exists)
				throw new WebApplicationException(
						responseGenerator.referenceTargetNotFoundLocally(bundleIndex, resource, resourceReference));
		}

		return false; // throws exception if reference could not be resolved
	}

	private Optional<Resource> resolveLiteralExternalReference(ResourceReference resourceReference)
	{
		Objects.requireNonNull(resourceReference, "resourceReference");
		if (!ReferenceType.LITERAL_EXTERNAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a literal external reference");

		String remoteServerBase = resourceReference.getServerBase(serverBase);
		Optional<FhirWebserviceClient> client = clientProvider.getClient(remoteServerBase);

		if (client.isEmpty())
		{
			logger.warn(
					"Error while resolving literal external reference {}, no remote client found for server base {}",
					resourceReference.getReference().getReference(), remoteServerBase);
			return Optional.empty();
		}
		else
		{
			IdType referenceId = new IdType(resourceReference.getReference().getReference());
			logger.debug("Trying to resolve literal external reference {}, at remote server {}",
					resourceReference.getReference().getReference(), remoteServerBase);

			if (!referenceId.hasVersionIdPart())
				return Optional.ofNullable(client.get().read(referenceId.getResourceType(), referenceId.getIdPart()));
			else
				return Optional.ofNullable(client.get().read(referenceId.getResourceType(), referenceId.getIdPart(),
						referenceId.getVersionIdPart()));
		}
	}

	@Override
	public boolean resolveLiteralExternalReference(Resource resource, ResourceReference resourceReference)
			throws WebApplicationException, IllegalArgumentException
	{
		return resolveLiteralExternalReference(resource, null, resourceReference);
	}

	@Override
	public boolean resolveLiteralExternalReference(Resource resource, Integer bundleIndex,
			ResourceReference resourceReference) throws WebApplicationException, IllegalArgumentException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(resourceReference, "resourceReference");
		if (!ReferenceType.LITERAL_EXTERNAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a literal external reference");

		String remoteServerBase = resourceReference.getServerBase(serverBase);
		Optional<FhirWebserviceClient> client = clientProvider.getClient(remoteServerBase);

		if (client.isEmpty())
		{
			logger.error(
					"Error while resolving literal external reference {}, no remote client found for server base {}",
					resourceReference.getReference().getReference(), remoteServerBase);
			throw new WebApplicationException(responseGenerator.noEndpointFoundForLiteralExternalReference(bundleIndex,
					resource, resourceReference));
		}
		else
		{
			IdType referenceId = new IdType(resourceReference.getReference().getReference());
			logger.debug("Trying to resolve literal external reference {}, at remote server {}",
					resourceReference.getReference().getReference(), remoteServerBase);
			if (!client.get().exists(referenceId))
			{
				logger.error(
						"Error while resolving literal external reference {}, resource could not be found on remote server {}",
						resourceReference.getReference().getReference(), remoteServerBase);
				throw new WebApplicationException(responseGenerator.referenceTargetNotFoundRemote(bundleIndex, resource,
						resourceReference, remoteServerBase));
			}
		}

		return true; // throws exception if reference could not be resolved
	}

	private Optional<Resource> resolveConditionalReference(User user, ResourceReference resourceReference)
	{
		Objects.requireNonNull(resourceReference, "resourceReference");
		if (!ReferenceType.CONDITIONAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a conditional reference");

		UriComponents condition = UriComponentsBuilder.fromUriString(resourceReference.getReference().getReference())
				.build();
		String path = condition.getPath();
		if (path == null || path.isBlank())
		{
			logger.warn("Bad conditional reference target '{}' of reference at {}",
					resourceReference.getReference().getReference(), resourceReference.getReferenceLocation());
			return Optional.empty();
		}

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(path);

		if (referenceDao.isEmpty())
		{
			logger.warn("Reference target type of reference at {} not supported by this implementation",
					resourceReference.getReferenceLocation());
			return Optional.empty();
		}
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
			{
				logger.warn("Reference target type of reference at {} not supported",
						resourceReference.getReferenceLocation());
				return Optional.empty();
			}

			return search(user, d, resourceReference, condition.getQueryParams(), true);
		}
	}

	@Override
	public boolean resolveConditionalReference(User user, Resource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException
	{
		return resolveConditionalReference(user, resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveConditionalReference(User user, Resource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException
	{
		Objects.requireNonNull(user, "user");
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

			Resource target = search(user, resource, bundleIndex, connection, d, resourceReference,
					condition.getQueryParams(), true);

			resourceReference.getReference().setIdentifier(null).setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		return true; // throws exception if reference could not be resolved
	}

	private Optional<Resource> resolveLogicalReference(User user, ResourceReference resourceReference)
	{
		Objects.requireNonNull(resourceReference, "resourceReference");
		if (!ReferenceType.LOGICAL.equals(resourceReference.getType(serverBase)))
			throw new IllegalArgumentException("Not a logical reference");

		String targetType = resourceReference.getReference().getType();

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(targetType);

		if (referenceDao.isEmpty())
		{
			logger.warn("Reference target type of reference at {} not supported by this implementation",
					resourceReference.getReferenceLocation());
			return Optional.empty();
		}
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!resourceReference.supportsType(d.getResourceType()))
			{
				logger.warn("Reference target type of reference at {} not supported by this implementation",
						resourceReference.getReferenceLocation());
				return Optional.empty();
			}

			Identifier targetIdentifier = resourceReference.getReference().getIdentifier();
			return search(user, d, resourceReference, Map.of("identifier",
					Collections.singletonList(targetIdentifier.getSystem() + "|" + targetIdentifier.getValue())), true);
		}
	}

	@Override
	public boolean resolveLogicalReference(User user, Resource resource, ResourceReference resourceReference,
			Connection connection) throws WebApplicationException, IllegalArgumentException
	{
		return resolveLogicalReference(user, resource, null, resourceReference, connection);
	}

	@Override
	public boolean resolveLogicalReference(User user, Resource resource, Integer bundleIndex,
			ResourceReference resourceReference, Connection connection)
			throws WebApplicationException, IllegalArgumentException
	{
		Objects.requireNonNull(user, "user");
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
			Resource target = search(user, resource, bundleIndex, connection, d, resourceReference, Map.of("identifier",
					Collections.singletonList(targetIdentifier.getSystem() + "|" + targetIdentifier.getValue())), true);

			resourceReference.getReference().setIdentifier(null).setReferenceElement(
					new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		return true; // throws exception if reference could not be resolved
	}

	private Optional<Resource> search(User user, ResourceDao<?> referenceTargetDao, ResourceReference resourceReference,
			Map<String, List<String>> queryParameters, boolean logicalNotConditional)
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

		SearchQuery<?> query = referenceTargetDao.createSearchQuery(user, 1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
		{
			String unsupportedQueryParametersString = unsupportedQueryParameters.stream()
					.map(SearchQueryParameterError::toString).collect(Collectors.joining("; "));

			logger.warn("{} reference {} at {} in resource contains unsupported queryparameter{} {}",
					logicalNotConditional ? "Logical" : "Conditional", queryParameters,
					resourceReference.getReferenceLocation(), unsupportedQueryParameters.size() != 1 ? "s" : "",
					unsupportedQueryParametersString);

			return Optional.empty();
		}

		PartialResult<?> result = exceptionHandler.handleSqlException(() -> referenceTargetDao.search(query));

		if (result.getOverallCount() <= 0)
		{
			if (logicalNotConditional)
				logger.warn("Reference target by identifier '{}|{}' of reference at {} in resource",
						resourceReference.getReference().getIdentifier().getSystem(),
						resourceReference.getReference().getIdentifier().getValue(),
						resourceReference.getReferenceLocation());
			else
				logger.warn("Reference target by identifier '{}|{}' of reference at {} in resource",
						resourceReference.getReference().getIdentifier().getSystem(),
						resourceReference.getReference().getIdentifier().getValue(),
						resourceReference.getReferenceLocation());
			return Optional.empty();
		}
		else if (result.getOverallCount() == 1)
		{
			return Optional.of(result.getPartialResult().get(0));
		}
		else // if (result.getOverallCount() > 1)
		{
			int overallCount = result.getOverallCount();

			if (logicalNotConditional)
				logger.warn(
						"Found {} matches for reference target by identifier '{}|{}' of reference at {} in resource",
						overallCount, resourceReference.getReference().getIdentifier().getSystem(),
						resourceReference.getReference().getIdentifier().getValue(),
						resourceReference.getReferenceLocation());
			else
				logger.warn("Found {} matches for reference target by condition '{}' of reference at {} in resource",
						overallCount, queryParameters, resourceReference.getReferenceLocation());

			return Optional.empty();
		}
	}

	private Resource search(User user, Resource resource, Integer bundleIndex, Connection connection,
			ResourceDao<?> referenceTargetDao, ResourceReference resourceReference,
			Map<String, List<String>> queryParameters, boolean logicalNotConditional)
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

		SearchQuery<?> query = referenceTargetDao.createSearchQuery(user, 1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
			throw new WebApplicationException(
					responseGenerator.badReference(logicalNotConditional, bundleIndex, resource, resourceReference,
							UriComponentsBuilder.newInstance()
									.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
							unsupportedQueryParameters));

		PartialResult<?> result = exceptionHandler
				.handleSqlException(() -> referenceTargetDao.searchWithTransaction(connection, query));

		if (result.getOverallCount() <= 0)
		{
			if (logicalNotConditional)
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
			if (logicalNotConditional)
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
