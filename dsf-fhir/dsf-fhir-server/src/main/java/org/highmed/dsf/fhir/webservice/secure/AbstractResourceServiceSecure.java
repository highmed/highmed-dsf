package org.highmed.dsf.fhir.webservice.secure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.BasicResourceService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public abstract class AbstractResourceServiceSecure<D extends ResourceDao<R>, R extends Resource, S extends BasicResourceService<R>>
		extends AbstractServiceSecure<S> implements BasicResourceService<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceSecure.class);

	protected final Class<R> resourceType;
	protected final String resourceTypeName;
	protected final String serverBase;
	protected final D dao;
	protected final ExceptionHandler exceptionHandler;
	protected final ParameterConverter parameterConverter;
	protected final AuthorizationRule<R> authorizationRule;

	public AbstractResourceServiceSecure(S delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, Class<R> resourceType, D dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<R> authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver);

		this.resourceType = resourceType;
		this.resourceTypeName = resourceType.getAnnotation(ResourceDef.class).name();
		this.serverBase = serverBase;
		this.dao = dao;
		this.exceptionHandler = exceptionHandler;
		this.parameterConverter = parameterConverter;
		this.authorizationRule = authorizationRule;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(resourceTypeName, "resourceTypeName");
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
		Objects.requireNonNull(authorizationRule, "authorizationRule");
	}

	@Override
	public Response create(R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Optional<String> reasonCreateAllowed = authorizationRule.reasonCreateAllowed(getCurrentUser(), resource);

		if (reasonCreateAllowed.isEmpty())
		{
			audit.info("Create of resource {} denied for user '{}'", resourceTypeName, getCurrentUser().getName());
			return forbidden("create");
		}
		else
		{
			audit.info("Create of resource {} allowed for user '{}'", resourceTypeName, getCurrentUser().getName());
			return delegate.create(resource, uri, headers);
		}
	}

	@Override
	public Response read(String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Response read = delegate.read(id, uri, headers);

		if (read.hasEntity() && resourceType.isInstance(read.getEntity()))
		{
			R entity = resourceType.cast(read.getEntity());
			Optional<String> reasonReadAllowed = authorizationRule.reasonReadAllowed(getCurrentUser(), entity);

			if (reasonReadAllowed.isEmpty())
			{
				audit.info("Read of resource {} denied for user '{}'", entity.getIdElement().getValue(),
						getCurrentUser().getName());
				return forbidden("read");
			}
			else
			{
				audit.info("Read of resource {} allowed for user '{}'", entity.getIdElement().getValue(),
						getCurrentUser().getName());
				return read;
			}
		}
		else if (read.hasEntity() && read.getEntity() instanceof OperationOutcome)
		{
			audit.info("Read of resource {} for user '{}' returned with OperationOutcome, status {}",
					resourceTypeName + "/" + id, getCurrentUser().getName(), read.getStatus());

			logger.info("Returning with OperationOutcome, status {}", read.getStatus());
			return read;
		}
		else if (read.hasEntity())
		{
			audit.info("Read of resource {} denied for user '{}', not a {}", resourceTypeName + "/" + id,
					getCurrentUser().getName(), resourceTypeName);

			logger.warn("Not allowing access to entity of type {}", read.getEntity().getClass().getName());
			return forbidden("read");
		}
		else
		{
			audit.info("Read of resource {} for user '{}' returned without entity, status {}",
					resourceTypeName + "/" + id, getCurrentUser().getName(), read.getStatus());

			logger.warn("Returning with status {}, but no entity", read.getStatus());
			return read;
		}
	}

	@Override
	public Response vread(String id, long version, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Response read = delegate.vread(id, version, uri, headers);

		if (read.hasEntity() && resourceType.isInstance(read.getEntity()))
		{
			R entity = resourceType.cast(read.getEntity());
			Optional<String> reasonReadAllowed = authorizationRule.reasonReadAllowed(getCurrentUser(), entity);

			if (reasonReadAllowed.isEmpty())
			{
				audit.info("Read of resource {} denied for user '{}'", entity.getIdElement().getValue(),
						getCurrentUser().getName());
				return forbidden("read");
			}
			else
			{
				audit.info("Read of resource {} allowed for user '{}'", entity.getIdElement().getValue(),
						getCurrentUser().getName());
				return read;
			}
		}
		else if (read.hasEntity() && read.getEntity() instanceof OperationOutcome)
		{
			audit.info("Read of resource {} for user '{}' returned with OperationOutcome, status {}",
					resourceTypeName + "/" + id + "/_history/" + version, getCurrentUser().getName(), read.getStatus());

			logger.info("Returning with OperationOutcome, status {}", read.getStatus());
			return read;
		}
		else if (read.hasEntity())
		{
			audit.info("Read of resource {} denied for user '{}', not a {}",
					resourceTypeName + "/" + id + "/_history/" + version, getCurrentUser().getName(), resourceTypeName);

			logger.warn("Not allowing access to entity of type {}", read.getEntity().getClass().getName());
			return forbidden("read");
		}
		else
		{
			audit.info("Read of resource {} for user '{}' returned without entity, status {}",
					resourceTypeName + "/" + id + "/_history/" + version, getCurrentUser().getName(), read.getStatus());

			logger.warn("Returning with status {}, but no entity", read.getStatus());
			return read;
		}
	}

	@Override
	public Response update(String id, R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Optional<R> dbResource = exceptionHandler.handleSqlAndResourceDeletedException(resourceTypeName,
				() -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));

		if (dbResource.isEmpty())
		{
			audit.info("Create as Update of non existing resource {} denied for user '{}'", resourceTypeName + "/" + id,
					getCurrentUser().getName());
			return responseGenerator.updateAsCreateNotAllowed(resourceTypeName, resourceTypeName + "/" + id);
		}
		else
			return update(id, resource, uri, headers, dbResource.get());
	}

	private Response update(String id, R newResource, UriInfo uri, HttpHeaders headers, R oldResource)
	{
		Optional<String> reasonUpdateAllowed = authorizationRule.reasonUpdateAllowed(getCurrentUser(), oldResource,
				newResource);

		if (reasonUpdateAllowed.isEmpty())
		{
			audit.info("Update of resource {} denied for user '{}'", oldResource.getIdElement().getValue(),
					getCurrentUser().getName());
			return forbidden("update");
		}
		else
		{
			audit.info("Update of resource {} allowed for user '{}'", oldResource.getIdElement().getValue(),
					getCurrentUser().getName());

			Response update = delegate.update(id, newResource, uri, headers);

			if (update.hasEntity() && !(update.getEntity() instanceof OperationOutcome))
				logger.warn("Update returned with entity of type {}", update.getEntity().getClass().getName());
			else
				logger.info("Update returned with status {}, but no entity", update.getStatus());

			return update;
		}
	}

	@Override
	public Response update(R resource, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Map<String, List<String>> queryParameters = uri.getQueryParameters();
		PartialResult<R> result = getExisting(queryParameters);

		// No matches, no id provided: The server creates the resource.
		if (result.getOverallCount() <= 0 && !resource.hasId())
		{
			// more security checks and audit log in create method
			return create(resource, uri, headers);
		}

		// No matches, id provided: The server treats the interaction as an Update as Create interaction (or rejects it,
		// if it does not support Update as Create) -> reject
		else if (result.getOverallCount() <= 0 && resource.hasId())
		{
			audit.info("Create as Update of non existing resource {} denied for user '{}'",
					resource.getIdElement().getValue(), getCurrentUser().getName());
			return responseGenerator.updateAsCreateNotAllowed(resourceTypeName, resource.getIdElement().getValue());
		}

		// One Match, no resource id provided OR (resource id provided and it matches the found resource):
		// The server performs the update against the matching resource
		else if (result.getOverallCount() == 1)
		{
			R dbResource = result.getPartialResult().get(0);
			IdType dbResourceId = dbResource.getIdElement();

			// update resource has no id
			if (!resource.hasId())
			{
				resource.setIdElement(dbResourceId);
				// more security checks and audit log in update method
				return update(resource.getIdElement().getIdPart(), resource, uri, headers, resource);
			}

			// update resource has same id
			else if (resource.hasId()
					&& (!resource.getIdElement().hasBaseUrl()
							|| serverBase.equals(resource.getIdElement().getBaseUrl()))
					&& (!resource.getIdElement().hasResourceType()
							|| resourceTypeName.equals(resource.getIdElement().getResourceType()))
					&& (dbResourceId.getIdPart().equals(resource.getIdElement().getIdPart())))
				// more security checks and audit log in update method
				return update(resource.getIdElement().getIdPart(), resource, uri, headers, resource);

			// update resource has different id -> 400 Bad Request
			else
			{
				// TODO audit log
				return responseGenerator.badRequestIdsNotMatching(
						dbResourceId.withServerBase(serverBase, resourceTypeName),
						resource.getIdElement().hasBaseUrl() && resource.getIdElement().hasResourceType()
								? resource.getIdElement()
								: resource.getIdElement().withServerBase(serverBase, resourceTypeName));
			}
		}

		// Multiple matches: The server returns a 412 Precondition Failed error indicating the client's criteria were
		// not selective enough preferably with an OperationOutcome
		else // if (result.getOverallCount() > 1)
		{
			// TODO audit log
			return responseGenerator.multipleExists(resourceTypeName, UriComponentsBuilder.newInstance()
					.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString());
		}
	}

	private PartialResult<R> getExisting(Map<String, List<String>> queryParameters)
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

		SearchQuery<R> query = dao.createSearchQuery(getCurrentUser(), 1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
		{
			// TODO audit log
			throw new WebApplicationException(responseGenerator.badRequest(
					UriComponentsBuilder.newInstance()
							.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
					unsupportedQueryParameters));
		}

		return exceptionHandler.handleSqlException(() -> dao.search(query));
	}

	@Override
	public Response delete(String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Optional<R> dbResource = exceptionHandler
				.handleSqlException(() -> dao.readIncludingDeleted(parameterConverter.toUuid(resourceTypeName, id)));

		if (dbResource.isPresent())
		{
			R oldResource = dbResource.get();

			Optional<String> reasonDeleteAllowed = reasonDeleteAllowed(oldResource);
			if (reasonDeleteAllowed.isEmpty())
			{
				audit.info("Delete of resource {} denied for user '{}'", oldResource.getIdElement().getValue(),
						getCurrentUser().getName());
				return forbidden("delete");
			}
			else
			{
				audit.info("Delete of resource {} allowed for user '{}'", oldResource.getIdElement().getValue(),
						getCurrentUser().getName());
				return delegate.delete(id, uri, headers);
			}
		}
		else
		{
			audit.info("Resource to delete {} not found for user '{}'", resourceTypeName + "/" + id,
					getCurrentUser().getName());
			return responseGenerator.notFound(id, resourceTypeName);
		}
	}

	/**
	 * Override this method for non default behavior. Default: Not allowed.
	 * 
	 * @param oldResource
	 * @return Reason as String in {@link Optional#of(Object)} if delete allowed
	 */
	protected Optional<String> reasonDeleteAllowed(R oldResource)
	{
		return Optional.empty();
	}

	@Override
	public Response delete(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Map<String, List<String>> queryParameters = uri.getQueryParameters();
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

		SearchQuery<R> query = dao.createSearchQuery(getCurrentUser(), 1, 1);
		query.configureParameters(queryParameters);

		List<SearchQueryParameterError> unsupportedQueryParameters = query
				.getUnsupportedQueryParameters(queryParameters);
		if (!unsupportedQueryParameters.isEmpty())
		{
			// TODO audit log
			return responseGenerator.badRequest(
					UriComponentsBuilder.newInstance()
							.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString(),
					unsupportedQueryParameters);
		}

		PartialResult<R> result = exceptionHandler.handleSqlException(() -> dao.search(query));

		// No matches
		if (result.getOverallCount() <= 0)
		{
			// TODO audit log
			return Response.noContent().build(); // TODO return OperationOutcome
		}

		// One Match: The server performs an ordinary delete on the matching resource
		else if (result.getOverallCount() == 1)
		{
			R resource = result.getPartialResult().get(0);

			// more security checks and audit log in delete method
			return delete(resource.getIdElement().getIdPart(), uri, headers);
		}

		// Multiple matches: A server may choose to delete all the matching resources, or it may choose to return a 412
		// Precondition Failed error indicating the client's criteria were not selective enough.
		else
		{
			// TODO audit log
			return responseGenerator.multipleExists(resourceTypeName, UriComponentsBuilder.newInstance()
					.replaceQueryParams(CollectionUtils.toMultiValueMap(queryParameters)).toUriString());
		}
	}

	@Override
	public Response search(UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		Optional<String> reasonSearchAllowed = authorizationRule.reasonSearchAllowed(getCurrentUser());
		if (reasonSearchAllowed.isEmpty())
		{
			audit.info("Search of resource {} denied for user '{}'", resourceTypeName, getCurrentUser().getName());
			return forbidden("search");
		}
		else
		{
			audit.info("Search of resource {} allowed for user '{}'", resourceTypeName, getCurrentUser().getName());
			return delegate.search(uri, headers);
		}
	}

	@Override
	public Response postValidateNew(String validate, Parameters parameters, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		return delegate.postValidateNew(validate, parameters, uri, headers);
	}

	@Override
	public Response getValidateNew(String validate, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		return delegate.getValidateNew(validate, uri, headers);
	}

	@Override
	public Response postValidateExisting(String validate, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		return delegate.postValidateExisting(validate, id, parameters, uri, headers);
	}

	@Override
	public Response getValidateExisting(String validate, String id, UriInfo uri, HttpHeaders headers)
	{
		logger.debug("Current user '{}', role '{}'", userProvider.getCurrentUser().getName(),
				userProvider.getCurrentUser().getRole());

		return delegate.getValidateExisting(validate, id, uri, headers);
	}
}
