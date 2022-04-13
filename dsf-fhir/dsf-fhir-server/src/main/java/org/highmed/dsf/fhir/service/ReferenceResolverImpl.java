package org.highmed.dsf.fhir.service;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.client.ClientProvider;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
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
import org.hl7.fhir.r4.model.OperationOutcome;
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
	public boolean referenceCanBeResolved(ResourceReference reference, Connection connection)
	{
		return referenceCanBeChecked(reference, connection);
	}

	@Override
	public boolean referenceCanBeChecked(ResourceReference reference, Connection connection)
	{
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(connection, "connection");

		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case LITERAL_EXTERNAL:
			case RELATED_ARTEFACT_LITERAL_EXTERNAL_URL:
			case ATTACHMENT_LITERAL_EXTERNAL_URL:
				return literalExternalReferenceCanBeCheckedAndResolved(reference);
			case LOGICAL:
				return logicalReferenceCanBeCheckedAndResolved(reference, connection);
			default:
				return true;
		}
	}

	private boolean logicalReferenceCanBeCheckedAndResolved(ResourceReference reference, Connection connection)
	{
		ReferenceType type = reference.getType(serverBase);
		if (!ReferenceType.LOGICAL.equals(type))
			throw new IllegalArgumentException("Not a logical reference");

		NamingSystemDao namingSystemDao = daoProvider.getNamingSystemDao();

		return exceptionHandler
				.handleSqlException(() -> namingSystemDao.existsWithUniqueIdUriEntryResolvable(connection,
						reference.getReference().getIdentifier().getSystem()));
	}

	private boolean literalExternalReferenceCanBeCheckedAndResolved(ResourceReference reference)
	{
		ReferenceType type = reference.getType(serverBase);
		if (!EnumSet.of(ReferenceType.LITERAL_EXTERNAL, ReferenceType.RELATED_ARTEFACT_LITERAL_EXTERNAL_URL,
				ReferenceType.ATTACHMENT_LITERAL_EXTERNAL_URL).contains(type))
			throw new IllegalArgumentException(
					"Not a literal external reference, related artifact literal external url or attachment literal external url");

		String remoteServerBase = reference.getServerBase(serverBase);
		return clientProvider.endpointExists(remoteServerBase);
	}

	@Override
	public Optional<Resource> resolveReference(User user, ResourceReference reference, Connection connection)
	{
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(connection, "connection");

		ReferenceType type = reference.getType(serverBase);
		switch (type)
		{
			case LITERAL_INTERNAL:
				return resolveLiteralInternalReference(reference, connection);
			case LITERAL_EXTERNAL:
			case RELATED_ARTEFACT_LITERAL_EXTERNAL_URL:
			case ATTACHMENT_LITERAL_EXTERNAL_URL:
				return resolveLiteralExternalReference(reference);
			case CONDITIONAL:
			case RELATED_ARTEFACT_CONDITIONAL_URL:
			case ATTACHMENT_CONDITIONAL_URL:
				return resolveConditionalReference(user, reference, connection);
			case LOGICAL:
				return resolveLogicalReference(user, reference, connection);
			default:
				throw new IllegalArgumentException("Reference of type " + type + " not supported");
		}
	}

	private void throwIfReferenceTypeUnexpected(ReferenceType type, ReferenceType expected)
	{
		if (!expected.equals(type))
			throw new IllegalArgumentException("ReferenceType " + expected + " expected, but was " + type);
	}

	private void throwIfReferenceTypeUnexpected(ReferenceType type, ReferenceType... expected)
	{
		if (!EnumSet.copyOf(Arrays.asList(expected)).contains(type))
			throw new IllegalArgumentException(
					"ReferenceTypes " + Arrays.toString(expected) + " expected, but was " + type);
	}

	private Optional<Resource> resolveLiteralInternalReference(ResourceReference reference, Connection connection)
	{
		Objects.requireNonNull(reference, "reference");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.LITERAL_INTERNAL);

		IdType id = new IdType(reference.getReference().getReference());
		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(id.getResourceType());

		if (referenceDao.isEmpty())
		{
			logger.warn("Reference target type of reference at {} not supported by this implementation",
					reference.getLocation());
			return Optional.empty();
		}
		else
		{
			@SuppressWarnings("unchecked")
			ResourceDao<Resource> d = (ResourceDao<Resource>) referenceDao.get();
			if (!reference.supportsType(d.getResourceType()))
			{
				logger.warn("Reference target type of reference at {} not supported", reference.getLocation());
				return Optional.empty();
			}

			Optional<UUID> uuid = parameterConverter.toUuid(id.getIdPart());

			if (!id.hasVersionIdPart())
				return uuid.flatMap(i -> exceptionHandler.catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(() ->
				{
					if (connection == null)
						return d.read(i);
					else
						return d.readWithTransaction(connection, i);
				}, Optional::empty, Optional::empty));
			else
				return uuid.flatMap(i -> exceptionHandler.catchAndLogSqlAndResourceDeletedExceptionAndIfReturn(() ->
				{
					if (connection == null)
						return d.readVersion(i, id.getVersionIdPartAsLong());
					else
						return d.readVersionWithTransaction(connection, i, id.getVersionIdPartAsLong());
				}, Optional::empty, Optional::empty));
		}
	}

	private Optional<Resource> resolveLiteralExternalReference(ResourceReference reference)
	{
		Objects.requireNonNull(reference, "reference");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.LITERAL_EXTERNAL,
				ReferenceType.RELATED_ARTEFACT_LITERAL_EXTERNAL_URL, ReferenceType.ATTACHMENT_LITERAL_EXTERNAL_URL);

		String remoteServerBase = reference.getServerBase(serverBase);
		Optional<FhirWebserviceClient> client = clientProvider.getClient(remoteServerBase);

		if (client.isEmpty())
		{
			logger.warn("Literal external reference {} could not be resolved, no remote client for server base {}",
					reference.getReference().getReference(), remoteServerBase);
			return Optional.empty();
		}
		else
		{
			IdType referenceId = new IdType(reference.getReference().getReference());
			logger.debug("Trying to resolve literal external reference {}, at remote server {}",
					reference.getReference().getReference(), remoteServerBase);

			try
			{
				if (!referenceId.hasVersionIdPart())
					return Optional
							.ofNullable(client.get().read(referenceId.getResourceType(), referenceId.getIdPart()));
				else
					return Optional.ofNullable(client.get().read(referenceId.getResourceType(), referenceId.getIdPart(),
							referenceId.getVersionIdPart()));
			}
			catch (Exception e)
			{
				logger.error("Literal external reference {} could not be resolved on remote server {}: {}",
						reference.getReference().getReference(), remoteServerBase, e.getMessage());

				if (logger.isDebugEnabled())
					logger.debug("Literal external reference " + reference.getReference().getReference()
							+ " could not be resolved on remote server " + remoteServerBase, e);
				return Optional.empty();
			}
		}
	}

	private Optional<Resource> resolveConditionalReference(User user, ResourceReference reference,
			Connection connection)
	{
		Objects.requireNonNull(reference, "reference");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.CONDITIONAL,
				ReferenceType.RELATED_ARTEFACT_CONDITIONAL_URL, ReferenceType.ATTACHMENT_CONDITIONAL_URL);

		String referenceValue = reference.getValue();
		String referenceLocation = reference.getLocation();

		UriComponents condition = UriComponentsBuilder.fromUriString(referenceValue).build();
		String path = condition.getPath();
		if (path == null || path.isBlank())
		{
			logger.warn("Bad conditional reference target '{}' of reference at {}", referenceValue, referenceLocation);
			return Optional.empty();
		}

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(path);

		if (referenceDao.isEmpty())
		{
			logger.warn("Reference target type of reference at {} not supported by this implementation",
					referenceLocation);
			return Optional.empty();
		}
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!reference.supportsType(d.getResourceType()))
			{
				logger.warn("Reference target type of reference at {} not supported", referenceLocation);
				return Optional.empty();
			}

			return search(user, connection, d, reference, condition.getQueryParams(), true);
		}
	}

	private Optional<Resource> resolveLogicalReference(User user, ResourceReference reference, Connection connection)
	{
		Objects.requireNonNull(reference, "reference");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.LOGICAL);

		String targetType = reference.getReference().getType();

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(targetType);

		if (referenceDao.isEmpty())
		{
			logger.warn("Reference target type of reference at {} not supported by this implementation",
					reference.getLocation());
			return Optional.empty();
		}
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!reference.supportsType(d.getResourceType()))
			{
				logger.warn("Reference target type of reference at {} not supported by this implementation",
						reference.getLocation());
				return Optional.empty();
			}

			Identifier targetIdentifier = reference.getReference().getIdentifier();
			return search(user, connection, d, reference, Map.of("identifier",
					Collections.singletonList(targetIdentifier.getSystem() + "|" + targetIdentifier.getValue())), true);
		}
	}

	private Optional<Resource> search(User user, Connection connection, ResourceDao<?> referenceTargetDao,
			ResourceReference resourceReference, Map<String, List<String>> queryParameters,
			boolean logicalNotConditional)
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
					logicalNotConditional ? "Logical" : "Conditional", queryParameters, resourceReference.getLocation(),
					unsupportedQueryParameters.size() != 1 ? "s" : "", unsupportedQueryParametersString);

			return Optional.empty();
		}

		PartialResult<?> result = exceptionHandler.handleSqlException(() ->
		{
			if (connection == null)
				return referenceTargetDao.search(query);
			else
				return referenceTargetDao.searchWithTransaction(connection, query);
		});

		if (result.getTotal() <= 0)
		{
			if (logicalNotConditional)
				logger.warn("Reference target by identifier '{}|{}' of reference at {} in resource",
						resourceReference.getReference().getIdentifier().getSystem(),
						resourceReference.getReference().getIdentifier().getValue(), resourceReference.getLocation());
			else
				logger.warn("Reference target by condition '{}' of reference at {} in resource",
						UriComponentsBuilder.newInstance().path(referenceTargetDao.getResourceTypeName())
								.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
						resourceReference.getLocation());
			return Optional.empty();
		}
		else if (result.getTotal() == 1)
		{
			return Optional.of(result.getPartialResult().get(0));
		}
		else // if (result.getOverallCount() > 1)
		{
			int overallCount = result.getTotal();

			if (logicalNotConditional)
				logger.warn(
						"Found {} matches for reference target by identifier '{}|{}' of reference at {} in resource",
						overallCount, resourceReference.getReference().getIdentifier().getSystem(),
						resourceReference.getReference().getIdentifier().getValue(), resourceReference.getLocation());
			else
				logger.warn("Found {} matches for reference target by condition '{}' of reference at {} in resource",
						overallCount,
						UriComponentsBuilder.newInstance().path(referenceTargetDao.getResourceTypeName())
								.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
						resourceReference.getLocation());

			return Optional.empty();
		}
	}

	@Override
	public Optional<OperationOutcome> checkLiteralInternalReference(Resource resource,
			ResourceReference resourceReference, Connection connection) throws IllegalArgumentException
	{
		return checkLiteralInternalReference(resource, resourceReference, connection, null);
	}

	@Override
	public Optional<OperationOutcome> checkLiteralInternalReference(Resource resource, ResourceReference reference,
			Connection connection, Integer bundleIndex) throws IllegalArgumentException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(connection, "connection");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.LITERAL_INTERNAL,
				ReferenceType.RELATED_ARTEFACT_LITERAL_INTERNAL_URL, ReferenceType.ATTACHMENT_LITERAL_INTERNAL_URL);

		IdType id = new IdType(reference.getValue());
		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(id.getResourceType());

		if (referenceDao.isEmpty())
			return Optional.of(responseGenerator.referenceTargetTypeNotSupportedByImplementation(bundleIndex, resource,
					reference));
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!reference.supportsType(d.getResourceType()))
				return Optional.of(
						responseGenerator.referenceTargetTypeNotSupportedByResource(bundleIndex, resource, reference));

			boolean exists = exceptionHandler.handleSqlException(
					() -> d.existsNotDeletedWithTransaction(connection, id.getIdPart(), id.getVersionIdPart()));
			if (!exists)
				return Optional.of(responseGenerator.referenceTargetNotFoundLocally(bundleIndex, resource, reference));
		}

		return Optional.empty();
	}

	@Override
	public Optional<OperationOutcome> checkLiteralExternalReference(Resource resource,
			ResourceReference resourceReference) throws IllegalArgumentException
	{
		return checkLiteralExternalReference(resource, resourceReference, null);
	}

	@Override
	public Optional<OperationOutcome> checkLiteralExternalReference(Resource resource, ResourceReference reference,
			Integer bundleIndex) throws IllegalArgumentException
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(reference, "reference");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.LITERAL_EXTERNAL,
				ReferenceType.RELATED_ARTEFACT_LITERAL_EXTERNAL_URL, ReferenceType.ATTACHMENT_LITERAL_EXTERNAL_URL);

		String remoteServerBase = reference.getServerBase(serverBase);
		String referenceValue = reference.getValue();
		Optional<FhirWebserviceClient> client = clientProvider.getClient(remoteServerBase);

		if (client.isEmpty())
		{
			logger.error("Literal external reference {} could not be resolved, no remote client for server base {}",
					referenceValue, remoteServerBase);
			return Optional
					.of(responseGenerator.noEndpointFoundForLiteralExternalReference(bundleIndex, resource, reference));
		}
		else
		{
			IdType referenceId = new IdType(referenceValue);
			logger.debug("Trying to resolve literal external reference {}, at remote server {}", referenceValue,
					remoteServerBase);

			try
			{
				if (client.get().exists(referenceId))
				{
					// resource exists - no error response
					return Optional.empty();
				}
				else
				{
					logger.warn(
							"Literal external reference {} could not be resolved, resource not found on remote server {}",
							referenceValue, remoteServerBase);
					return Optional.of(responseGenerator.referenceTargetNotFoundRemote(bundleIndex, resource, reference,
							remoteServerBase));
				}
			}
			catch (Exception e)
			{
				logger.error("Literal external reference {} could not be resolved on remote server {}: {}",
						referenceValue, remoteServerBase, e.getMessage());

				if (logger.isDebugEnabled())
					logger.debug("Literal external reference " + referenceValue
							+ " could not be resolved on remote server " + remoteServerBase, e);

				return Optional.of(responseGenerator.referenceTargetCouldNotBeResolvedOnRemote(bundleIndex, resource,
						reference, remoteServerBase));
			}
		}
	}

	@Override
	public Optional<OperationOutcome> checkConditionalReference(User user, Resource resource,
			ResourceReference reference, Connection connection, Integer bundleIndex) throws IllegalArgumentException
	{
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(connection, "connection");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.CONDITIONAL);

		UriComponents condition = UriComponentsBuilder.fromUriString(reference.getReference().getReference()).build();
		String path = condition.getPath();
		if (path == null || path.isBlank())
			return Optional.of(responseGenerator.referenceTargetBadCondition(bundleIndex, resource, reference));

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(path);

		if (referenceDao.isEmpty())
			return Optional.of(responseGenerator.referenceTargetTypeNotSupportedByImplementation(bundleIndex, resource,
					reference));
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!reference.supportsType(d.getResourceType()))
				return Optional.of(
						responseGenerator.referenceTargetTypeNotSupportedByResource(bundleIndex, resource, reference));

			// Resource target =
			return search(user, resource, bundleIndex, connection, d, reference, condition.getQueryParams(), true);

			// TODO add literal reference for conditional reference somewhere else
			// reference.getReference().setIdentifier(null).setReferenceElement(
			// new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		// return Optional.empty();
	}

	@Override
	public Optional<OperationOutcome> checkLogicalReference(User user, Resource resource,
			ResourceReference resourceReference, Connection connection) throws IllegalArgumentException
	{
		return checkLogicalReference(user, resource, resourceReference, connection, null);
	}

	@Override
	public Optional<OperationOutcome> checkLogicalReference(User user, Resource resource, ResourceReference reference,
			Connection connection, Integer bundleIndex) throws IllegalArgumentException
	{
		Objects.requireNonNull(user, "user");
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(connection, "connection");
		throwIfReferenceTypeUnexpected(reference.getType(serverBase), ReferenceType.LOGICAL);

		String targetType = reference.getReference().getType();

		Optional<ResourceDao<?>> referenceDao = daoProvider.getDao(targetType);

		if (referenceDao.isEmpty())
			return Optional.of(responseGenerator.referenceTargetTypeNotSupportedByImplementation(bundleIndex, resource,
					reference));
		else
		{
			ResourceDao<?> d = referenceDao.get();
			if (!reference.supportsType(d.getResourceType()))
				return Optional.of(
						responseGenerator.referenceTargetTypeNotSupportedByResource(bundleIndex, resource, reference));

			Identifier targetIdentifier = reference.getReference().getIdentifier();
			// Resource target =
			return search(user, resource, bundleIndex, connection, d, reference, Map.of("identifier",
					Collections.singletonList(targetIdentifier.getSystem() + "|" + targetIdentifier.getValue())), true);

			// resourceReference.getReference().setIdentifier(null).setReferenceElement(
			// new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));

			// TODO add literal reference for logical reference somewhere else
			// reference.getReference().setReferenceElement(
			// new IdType(target.getResourceType().name(), target.getIdElement().getIdPart()));
		}

		// return Optional.empty();
	}

	private Optional<OperationOutcome> search(User user, Resource resource, Integer bundleIndex, Connection connection,
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
			return Optional
					.of(responseGenerator.badReference(logicalNotConditional, bundleIndex, resource, resourceReference,
							UriComponentsBuilder.newInstance()
									.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
							unsupportedQueryParameters));

		PartialResult<?> result = exceptionHandler
				.handleSqlException(() -> referenceTargetDao.searchWithTransaction(connection, query));

		if (result.getTotal() <= 0)
		{
			if (logicalNotConditional)
				return Optional.of(responseGenerator.referenceTargetNotFoundLocallyByIdentifier(bundleIndex, resource,
						resourceReference));
			else
				return Optional.of(responseGenerator.referenceTargetNotFoundLocallyByCondition(bundleIndex, resource,
						resourceReference));
		}
		else if (result.getTotal() == 1)
		{
			// return result.getPartialResult().get(0);
			return Optional.empty();
		}
		else // if (result.getOverallCount() > 1)
		{
			if (logicalNotConditional)
				return Optional.of(responseGenerator.referenceTargetMultipleMatchesLocallyByIdentifier(bundleIndex,
						resource, resourceReference, result.getTotal()));
			else
				return Optional.of(responseGenerator.referenceTargetMultipleMatchesLocallyByCondition(bundleIndex,
						resource, resourceReference, result.getTotal()));
		}
	}
}