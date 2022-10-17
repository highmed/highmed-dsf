package org.highmed.dsf.fhir.webservice.secure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.highmed.dsf.fhir.validation.ResourceValidator;
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
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public abstract class AbstractResourceServiceSecure<D extends ResourceDao<R>, R extends Resource, S extends BasicResourceService<R>>
		extends AbstractServiceSecure<S> implements BasicResourceService<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractResourceServiceSecure.class);

	protected final ReferenceCleaner referenceCleaner;
	protected final ReferenceExtractor referenceExtractor;
	protected final Class<R> resourceType;
	protected final String resourceTypeName;
	protected final String serverBase;
	protected final D dao;
	protected final ExceptionHandler exceptionHandler;
	protected final ParameterConverter parameterConverter;
	protected final AuthorizationRule<R> authorizationRule;
	protected final ResourceValidator resourceValidator;

	public AbstractResourceServiceSecure(S delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, Class<R> resourceType, D dao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<R> authorizationRule,
			ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver);

		this.referenceCleaner = referenceCleaner;
		this.referenceExtractor = referenceExtractor;
		this.resourceType = resourceType;
		this.resourceTypeName = resourceType.getAnnotation(ResourceDef.class).name();
		this.serverBase = serverBase;
		this.dao = dao;
		this.exceptionHandler = exceptionHandler;
		this.parameterConverter = parameterConverter;
		this.authorizationRule = authorizationRule;
		this.resourceValidator = resourceValidator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(referenceCleaner, "referenceCleaner");
		Objects.requireNonNull(referenceExtractor, "referenceExtractor");
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(resourceTypeName, "resourceTypeName");
		Objects.requireNonNull(serverBase, "serverBase");
		Objects.requireNonNull(dao, "dao");
		Objects.requireNonNull(exceptionHandler, "exceptionHandler");
		Objects.requireNonNull(parameterConverter, "parameterConverter");
		Objects.requireNonNull(authorizationRule, "authorizationRule");
		Objects.requireNonNull(resourceValidator, "resourceValidator");
	}

	private String toValidationLogMessage(ValidationResult validationResult)
	{
		return validationResult
				.getMessages().stream().map(m -> m.getLocationString() + " " + m.getLocationLine() + ":"
						+ m.getLocationCol() + " - " + m.getSeverity() + ": " + m.getMessage())
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private Response withResourceValidation(R resource, UriInfo uri, HttpHeaders headers, String method,
			Supplier<Response> delegate)
	{
		// FIXME hapi parser bug workaround
		referenceCleaner.cleanReferenceResourcesIfBundle(resource);

		ValidationResult validationResult = resourceValidator.validate(resource);

		if (validationResult.getMessages().stream().anyMatch(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())))
		{
			logger.warn("{} of {} unauthorized, resource not valid: {}", method, resource.fhirType(),
					toValidationLogMessage(validationResult));

			OperationOutcome outcome = new OperationOutcome();
			validationResult.populateOperationOutcome(outcome);
			return responseGenerator.response(Status.FORBIDDEN, outcome,
					parameterConverter.getMediaTypeThrowIfNotSupported(uri, headers)).build();
		}
		else
		{
			if (!validationResult.getMessages().isEmpty())
				logger.warn("Resource {} validated with messages: {}", resource.fhirType(),
						toValidationLogMessage(validationResult));

			return delegate.get();
		}
	}

	@Override
	public Response create(R resource, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		resolveLiteralInternalRelatedArtifactOrAttachmentUrls(resource);

		Optional<String> reasonCreateAllowed = authorizationRule.reasonCreateAllowed(getCurrentUser(), resource);

		if (reasonCreateAllowed.isEmpty())
		{
			audit.info("Create of resource {} denied for user '{}' ({})", resourceTypeName, getCurrentUser().getName(),
					getCurrentUser().getSubjectDn());
			return forbidden("create");
		}
		else
		{
			return withResourceValidation(resource, uri, headers, "Create", () ->
			{
				audit.info("Create of resource {} allowed for user '{}' ({}): {}", resourceTypeName,
						getCurrentUser().getName(), getCurrentUser().getSubjectDn(), reasonCreateAllowed.get());

				Response created = delegate.create(resource, uri, headers);

				if (created.hasEntity() && !resourceType.isInstance(created.getEntity())
						&& !(created.getEntity() instanceof OperationOutcome))
					logger.warn("Create returned with entity of type {}", created.getEntity().getClass().getName());
				else if (!created.hasEntity()
						&& !PreferReturnType.MINIMAL.equals(parameterConverter.getPreferReturn(headers)))
					logger.warn("Create returned with status {}, but no entity", created.getStatus());

				return created;
			});
		}
	}

	private void resolveLiteralInternalRelatedArtifactOrAttachmentUrls(R resource)
	{
		if (resource == null)
			return;

		referenceExtractor.getReferences(resource)
				.filter(ref -> ReferenceType.RELATED_ARTEFACT_LITERAL_INTERNAL_URL.equals(ref.getType(serverBase))
						|| ReferenceType.ATTACHMENT_LITERAL_INTERNAL_URL.equals(ref.getType(serverBase)))
				.forEach(this::resolveLiteralInternalRelatedArtifactOrAttachmentUrl);
	}

	private void resolveLiteralInternalRelatedArtifactOrAttachmentUrl(ResourceReference reference)
	{
		if (reference.hasRelatedArtifact() || reference.hasAttachment())
		{
			IdType newId = new IdType(reference.getValue());
			String absoluteUrl = newId.withServerBase(serverBase, newId.getResourceType()).getValue();

			if (reference.hasRelatedArtifact())
				reference.getRelatedArtifact().setUrl(absoluteUrl);
			else if (reference.hasAttachment())
				reference.getAttachment().setUrl(absoluteUrl);
		}
	}

	@Override
	public Response read(String id, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		Response read = delegate.read(id, uri, headers);

		if (read.hasEntity() && resourceType.isInstance(read.getEntity()))
		{
			R entity = resourceType.cast(read.getEntity());
			Optional<String> reasonReadAllowed = authorizationRule.reasonReadAllowed(getCurrentUser(), entity);

			if (reasonReadAllowed.isEmpty())
			{
				audit.info("Read of resource {} denied for user '{}' ({})", entity.getIdElement().getValue(),
						getCurrentUser().getName(), getCurrentUser().getSubjectDn());
				return forbidden("read");
			}
			else
			{
				audit.info("Read of resource {} allowed for user '{}' ({}): {}", entity.getIdElement().getValue(),
						getCurrentUser().getName(), getCurrentUser().getSubjectDn(), reasonReadAllowed.get());
				return read;
			}
		}
		else if (read.hasEntity() && read.getEntity() instanceof OperationOutcome)
		{
			audit.info("Read of resource {} for user '{}' ({}) returned with OperationOutcome, status {}",
					resourceTypeName + "/" + id, getCurrentUser().getName(), getCurrentUser().getSubjectDn(),
					read.getStatus());

			logger.info("Returning with OperationOutcome, status {}", read.getStatus());
			return read;
		}
		else if (read.hasEntity())
		{
			audit.info("Read of resource {} denied for user '{}' ({}), not a {}", resourceTypeName + "/" + id,
					getCurrentUser().getName(), getCurrentUser().getSubjectDn(), resourceTypeName);
			return forbidden("read");
		}
		else if (!read.hasEntity() && Status.NOT_MODIFIED.getStatusCode() == read.getStatus())
		{
			Optional<R> dbResource = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
					() -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));
			Optional<String> reasonReadAllowed = authorizationRule.reasonReadAllowed(getCurrentUser(),
					dbResource.get());

			if (reasonReadAllowed.isEmpty())
			{
				audit.info("Read of resource {} denied for user '{}' ({})", dbResource.get().getIdElement().getValue(),
						getCurrentUser().getName(), getCurrentUser().getSubjectDn());
				return forbidden("read");
			}
			else
			{
				audit.info("Read of resource {} allowed for user '{}' ({}): {}",
						dbResource.get().getIdElement().getValue(), getCurrentUser().getName(),
						getCurrentUser().getSubjectDn(), reasonReadAllowed.get());
				return read;
			}
		}
		else
		{
			audit.info("Read of resource {} for user '{}' ({}) returned without entity, status {}",
					resourceTypeName + "/" + id, getCurrentUser().getName(), getCurrentUser().getSubjectDn(),
					read.getStatus());

			logger.info("Returning with status {}, but no entity", read.getStatus());
			return read;
		}
	}

	@Override
	public Response vread(String id, long version, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		Response read = delegate.vread(id, version, uri, headers);

		if (read.hasEntity() && resourceType.isInstance(read.getEntity()))
		{
			R entity = resourceType.cast(read.getEntity());
			Optional<String> reasonReadAllowed = authorizationRule.reasonReadAllowed(getCurrentUser(), entity);

			if (reasonReadAllowed.isEmpty())
			{
				audit.info("Read of resource {} denied for user '{}' ({})", entity.getIdElement().getValue(),
						getCurrentUser().getName(), getCurrentUser().getSubjectDn());
				return forbidden("read");
			}
			else
			{
				audit.info("Read of resource {} allowed for user '{}' ({}): {}", entity.getIdElement().getValue(),
						getCurrentUser().getName(), getCurrentUser().getSubjectDn(), reasonReadAllowed.get());
				return read;
			}
		}
		else if (read.hasEntity() && read.getEntity() instanceof OperationOutcome)
		{
			audit.info("Read of resource {} for user '{}' ({}) returned with OperationOutcome, status {}",
					resourceTypeName + "/" + id + "/_history/" + version, getCurrentUser().getName(),
					getCurrentUser().getSubjectDn(), read.getStatus());

			logger.info("Returning with OperationOutcome, status {}", read.getStatus());
			return read;
		}
		else if (read.hasEntity())
		{
			audit.info("Read of resource {} denied for user '{}' ({}), not a {}",
					resourceTypeName + "/" + id + "/_history/" + version, getCurrentUser().getName(),
					getCurrentUser().getSubjectDn(), resourceTypeName);
			return forbidden("read");
		}
		else if (!read.hasEntity() && Status.NOT_MODIFIED.getStatusCode() == read.getStatus())
		{
			Optional<R> dbResource = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
					() -> dao.readVersion(parameterConverter.toUuid(resourceTypeName, id), version));
			Optional<String> reasonReadAllowed = authorizationRule.reasonReadAllowed(getCurrentUser(),
					dbResource.get());

			if (reasonReadAllowed.isEmpty())
			{
				audit.info("Read of resource {} denied for user '{}' ({})", dbResource.get().getIdElement().getValue(),
						getCurrentUser().getName(), getCurrentUser().getSubjectDn());
				return forbidden("read");
			}
			else
			{
				audit.info("Read of resource {} allowed for user '{}' ({}): {}",
						dbResource.get().getIdElement().getValue(), getCurrentUser().getName(),
						getCurrentUser().getSubjectDn(), reasonReadAllowed.get());
				return read;
			}
		}
		else
		{
			audit.info("Read of resource {} for user '{}' ({}) returned without entity, status {}",
					resourceTypeName + "/" + id + "/_history/" + version, getCurrentUser().getName(),
					getCurrentUser().getSubjectDn(), read.getStatus());

			logger.info("Returning with status {}, but no entity", read.getStatus());
			return read;
		}
	}

	@Override
	public Response history(UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		Optional<String> reasonHistoryAllowed = authorizationRule.reasonHistoryAllowed(getCurrentUser());
		if (reasonHistoryAllowed.isEmpty())
		{
			audit.info("History of resource {} denied for user '{}' ({})", resourceTypeName, getCurrentUser().getName(),
					getCurrentUser().getSubjectDn());
			return forbidden("search");
		}
		else
		{
			audit.info("History of resource {} allowed for user '{}' ({}): {}", resourceTypeName,
					getCurrentUser().getName(), getCurrentUser().getSubjectDn(), reasonHistoryAllowed.get());
			return delegate.history(uri, headers);
		}
	}

	@Override
	public Response history(String id, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		Optional<String> reasonHistoryAllowed = authorizationRule.reasonHistoryAllowed(getCurrentUser());
		if (reasonHistoryAllowed.isEmpty())
		{
			audit.info("History of resource {}/{} denied for user '{}' ({})", resourceTypeName, id,
					getCurrentUser().getName(), getCurrentUser().getSubjectDn());
			return forbidden("search");
		}
		else
		{
			audit.info("History of resource {}/{} allowed for user '{}' ({}): {}", resourceTypeName, id,
					getCurrentUser().getName(), getCurrentUser().getSubjectDn(), reasonHistoryAllowed.get());
			return delegate.history(id, uri, headers);
		}
	}

	@Override
	public Response update(String id, R resource, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		Optional<R> dbResource = exceptionHandler.handleSqlAndResourceDeletedException(serverBase, resourceTypeName,
				() -> dao.read(parameterConverter.toUuid(resourceTypeName, id)));

		if (dbResource.isEmpty())
		{
			audit.info("Create as Update of non existing resource {} denied for user '{}' ({})",
					resourceTypeName + "/" + id, getCurrentUser().getName(), getCurrentUser().getSubjectDn());
			return responseGenerator.updateAsCreateNotAllowed(resourceTypeName, resourceTypeName + "/" + id);
		}
		else
		{
			R cleanedResource = referenceCleaner.cleanLiteralReferences(dbResource.get());
			return update(id, resource, uri, headers, cleanedResource);
		}
	}

	private Response update(String id, R newResource, UriInfo uri, HttpHeaders headers, R oldResource)
	{
		resolveLiteralInternalRelatedArtifactOrAttachmentUrls(newResource);

		Optional<String> reasonUpdateAllowed = authorizationRule.reasonUpdateAllowed(getCurrentUser(), oldResource,
				newResource);

		if (reasonUpdateAllowed.isEmpty())
		{
			audit.info("Update of resource {} denied for user '{}' ({})", oldResource.getIdElement().getValue(),
					getCurrentUser().getName(), getCurrentUser().getSubjectDn());
			return forbidden("update");
		}
		else
		{
			return withResourceValidation(newResource, uri, headers, "Update", () ->
			{
				audit.info("Update of resource {} allowed for user '{}' ({}): {}",
						oldResource.getIdElement().getValue(), getCurrentUser().getName(),
						getCurrentUser().getSubjectDn(), reasonUpdateAllowed.get());

				Response updated = delegate.update(id, newResource, uri, headers);

				if (updated.hasEntity() && !resourceType.isInstance(updated.getEntity())
						&& !(updated.getEntity() instanceof OperationOutcome))
					logger.warn("Update returned with entity of type {}", updated.getEntity().getClass().getName());
				else if (!updated.hasEntity()
						&& !PreferReturnType.MINIMAL.equals(parameterConverter.getPreferReturn(headers)))
					logger.warn("Update returned with status {}, but no entity", updated.getStatus());

				return updated;
			});
		}
	}

	@Override
	public Response update(R resource, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		Map<String, List<String>> queryParameters = uri.getQueryParameters();
		PartialResult<R> result = getExisting(queryParameters);

		// No matches, no id provided: The server creates the resource.
		if (result.getTotal() <= 0 && !resource.hasId())
		{
			// more security checks and audit log in create method
			return create(resource, uri, headers);
		}

		// No matches, id provided: The server treats the interaction as an Update as Create interaction (or rejects it,
		// if it does not support Update as Create) -> reject
		else if (result.getTotal() <= 0 && resource.hasId())
		{
			audit.info("Create as Update of non existing resource {} denied for user '{}' ({})",
					resource.getIdElement().getValue(), getCurrentUser().getName(), getCurrentUser().getSubjectDn());
			return responseGenerator.updateAsCreateNotAllowed(resourceTypeName, resource.getIdElement().getValue());
		}

		// One Match, no resource id provided OR (resource id provided and it matches the found resource):
		// The server performs the update against the matching resource
		else if (result.getTotal() == 1)
		{
			R dbResource = result.getPartialResult().get(0);
			IdType dbResourceId = dbResource.getIdElement();

			// update: resource has no id
			if (!resource.hasId())
			{
				resource.setIdElement(dbResourceId);
				// more security checks and audit log in update method
				return update(resource.getIdElement().getIdPart(), resource, uri, headers, resource);
			}

			// update: resource has same id
			else if (resource.hasId()
					&& (!resource.getIdElement().hasBaseUrl()
							|| serverBase.equals(resource.getIdElement().getBaseUrl()))
					&& (!resource.getIdElement().hasResourceType()
							|| resourceTypeName.equals(resource.getIdElement().getResourceType()))
					&& (dbResourceId.getIdPart().equals(resource.getIdElement().getIdPart())))
			{
				// more security checks and audit log in update method
				return update(resource.getIdElement().getIdPart(), resource, uri, headers, resource);
			}

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

		SearchQuery<R> query = dao.createSearchQueryWithoutUserFilter(1, 1);
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
		logCurrentUser();

		Optional<R> dbResource = exceptionHandler
				.handleSqlException(() -> dao.readIncludingDeleted(parameterConverter.toUuid(resourceTypeName, id)));

		if (dbResource.isPresent())
		{
			R oldResource = dbResource.get();

			Optional<String> reasonDeleteAllowed = authorizationRule.reasonDeleteAllowed(getCurrentUser(), oldResource);
			if (reasonDeleteAllowed.isEmpty())
			{
				audit.info("Delete of resource {} denied for user '{}' ({})", oldResource.getIdElement().getValue(),
						getCurrentUser().getName(), getCurrentUser().getSubjectDn());
				return forbidden("delete");
			}
			else
			{
				audit.info("Delete of resource {} allowed for user '{}' ({}): {}",
						oldResource.getIdElement().getValue(), getCurrentUser().getName(),
						getCurrentUser().getSubjectDn(), reasonDeleteAllowed.get());
				return delegate.delete(id, uri, headers);
			}
		}
		else
		{
			audit.info("Resource to delete {} not found for user '{}' ({})", resourceTypeName + "/" + id,
					getCurrentUser().getName(), getCurrentUser().getSubjectDn());
			return responseGenerator.notFound(id, resourceTypeName);
		}
	}

	@Override
	public Response delete(UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

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
		if (result.getTotal() <= 0)
		{
			// TODO audit log
			return Response.noContent().build(); // TODO return OperationOutcome
		}

		// One Match: The server performs an ordinary delete on the matching resource
		else if (result.getTotal() == 1)
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
		logCurrentUser();

		Optional<String> reasonSearchAllowed = authorizationRule.reasonSearchAllowed(getCurrentUser());
		if (reasonSearchAllowed.isEmpty())
		{
			audit.info("Search of resource {} denied for user '{}' ({})", resourceTypeName, getCurrentUser().getName(),
					getCurrentUser().getSubjectDn());
			return forbidden("search");
		}
		else
		{
			audit.info("Search of resource {} allowed for user '{}' ({}): {}", resourceTypeName,
					getCurrentUser().getName(), getCurrentUser().getSubjectDn(), reasonSearchAllowed.get());
			return delegate.search(uri, headers);
		}
	}

	@Override
	public Response postValidateNew(String validate, Parameters parameters, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		return delegate.postValidateNew(validate, parameters, uri, headers);
	}

	@Override
	public Response getValidateNew(String validate, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		return delegate.getValidateNew(validate, uri, headers);
	}

	@Override
	public Response postValidateExisting(String validate, String id, Parameters parameters, UriInfo uri,
			HttpHeaders headers)
	{
		logCurrentUser();

		return delegate.postValidateExisting(validate, id, parameters, uri, headers);
	}

	@Override
	public Response getValidateExisting(String validate, String id, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		return delegate.getValidateExisting(validate, id, uri, headers);
	}

	private void logCurrentUser()
	{
		User user = getCurrentUser();
		logger.debug("Current user '{}' ({}), role '{}'", user.getName(), user.getSubjectDn(), user.getRole());
	}

	@Override
	public Response deletePermanently(String deletePath, String id, UriInfo uri, HttpHeaders headers)
	{
		logCurrentUser();

		Optional<R> dbResource = exceptionHandler
				.handleSqlException(() -> dao.readIncludingDeleted(parameterConverter.toUuid(resourceTypeName, id)));

		if (dbResource.isPresent())
		{
			R oldResource = dbResource.get();

			Optional<String> reasonDeleteAllowed = authorizationRule.reasonPermanentDeleteAllowed(getCurrentUser(),
					oldResource);
			if (reasonDeleteAllowed.isEmpty())
			{
				audit.info("Permanent delete of resource {} denied for user '{}'",
						oldResource.getIdElement().getValue(), getCurrentUser().getName());
				return forbidden("delete");
			}
			else
			{
				audit.info("Permanent delete of resource {} allowed for user '{}': {}",
						oldResource.getIdElement().getValue(), getCurrentUser().getName(), reasonDeleteAllowed.get());
				return delegate.deletePermanently(deletePath, id, uri, headers);
			}
		}
		else
		{
			audit.info("Resource to permanently delete {} not found for user '{}'", resourceTypeName + "/" + id,
					getCurrentUser().getName());
			return responseGenerator.notFound(id, resourceTypeName);
		}
	}
}
