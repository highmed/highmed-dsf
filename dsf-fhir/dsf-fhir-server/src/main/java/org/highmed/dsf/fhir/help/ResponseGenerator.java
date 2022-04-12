package org.highmed.dsf.fhir.help;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.history.History;
import org.highmed.dsf.fhir.history.HistoryEntry;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.search.PageAndCount;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQueryParameterError;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.validation.ValidationResult;

public class ResponseGenerator
{
	private static final Logger logger = LoggerFactory.getLogger(ResponseGenerator.class);

	private final String serverBase;

	public ResponseGenerator(String serverBase)
	{
		this.serverBase = serverBase;
	}

	public OperationOutcome createOutcome(IssueSeverity severity, IssueType type, String diagnostics)
	{
		OperationOutcome outcome = new OperationOutcome();
		outcome.getIssueFirstRep().setSeverity(severity);
		outcome.getIssueFirstRep().setCode(type);
		outcome.getIssueFirstRep().setDiagnostics(diagnostics);
		return outcome;
	}

	public OperationOutcome resourceDeleted(String resourceTypeName, String id)
	{
		return createOutcome(IssueSeverity.INFORMATION, IssueType.INFORMATIONAL,
				resourceTypeName + " with id " + id + " marked as deleted");
	}

	public OperationOutcome resourceDeletedPermanently(String resourceTypeName, String id)
	{
		return createOutcome(IssueSeverity.INFORMATION, IssueType.INFORMATIONAL,
				resourceTypeName + " with id " + id + " permanently deleted");
	}

	public ResponseBuilder response(Status status, Resource resource, MediaType mediaType)
	{
		return response(status, resource, mediaType, PreferReturnType.REPRESENTATION, null);
	}

	public ResponseBuilder response(Status status, Resource resource, MediaType mediaType, PreferReturnType prefer,
			Supplier<OperationOutcome> operationOutcomeCreator)
	{
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(resource, "resource");

		ResponseBuilder b = Response.status(status);

		switch (prefer)
		{
			case REPRESENTATION:
				b = b.entity(resource);
				break;
			case OPERATION_OUTCOME:
				b = b.entity(operationOutcomeCreator.get());
				break;
			case MINIMAL:
				// do nothing, headers only
				break;
			default:
				throw new RuntimeException(PreferReturnType.class.getName() + " value " + prefer + " not supported");
		}

		if (mediaType != null)
			b = b.type(mediaType.withCharset(StandardCharsets.UTF_8.displayName()));

		if (resource.getMeta() != null && resource.getMeta().getLastUpdated() != null
				&& resource.getMeta().getVersionId() != null)
		{
			b = b.lastModified(resource.getMeta().getLastUpdated());
			b = b.tag(new EntityTag(resource.getMeta().getVersionId(), true));
		}

		return b;
	}

	public OperationOutcome created(URI location, Resource resource)
	{
		return created(location.toString(), resource);
	}

	public OperationOutcome created(String location, Resource resource)
	{
		String message = String.format("%s created at location %s", resource.getResourceType().name(), location);
		return createOutcome(IssueSeverity.INFORMATION, IssueType.INFORMATIONAL, message);
	}

	public OperationOutcome updated(URI location, Resource resource)
	{
		return updated(location.toString(), resource);
	}

	public OperationOutcome updated(String location, Resource resource)
	{
		String message = String.format("%s updated at location %s", resource.getResourceType().name(), location);
		return createOutcome(IssueSeverity.INFORMATION, IssueType.INFORMATIONAL, message);
	}

	/**
	 * @param result
	 *            not <code>null</code>
	 * @param errors
	 *            not <code>null</code>
	 * @param bundleUri
	 *            not <code>null</code>
	 * @param format
	 *            may be <code>null</code>
	 * @param pretty
	 *            may be <code>null</code>
	 * @return {@link Bundle} of type {@link BundleType#SEARCHSET}
	 */
	public Bundle createSearchSet(PartialResult<? extends Resource> result, List<SearchQueryParameterError> errors,
			UriBuilder bundleUri, String format, String pretty)
	{
		Bundle bundle = new Bundle();
		bundle.setTimestamp(new Date());
		bundle.setType(BundleType.SEARCHSET);
		result.getPartialResult().stream().map(r -> toBundleEntryComponent(r, SearchEntryMode.MATCH))
				.forEach(bundle::addEntry);
		result.getIncludes().stream().map(r -> toBundleEntryComponent(r, SearchEntryMode.INCLUDE))
				.forEach(bundle::addEntry);
		if (!errors.isEmpty())
			bundle.addEntry(toBundleEntryComponent(toOperationOutcomeWarning(errors), SearchEntryMode.OUTCOME));

		bundle.setTotal(result.getTotal());

		setLinks(result.getPageAndCount(), bundleUri, format, pretty, bundle, result.getPartialResult().isEmpty(),
				result.getTotal());

		return bundle;
	}

	public BundleEntryComponent toBundleEntryComponent(Resource resource, SearchEntryMode mode)
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.getSearch().setMode(mode);
		entry.setResource(resource);
		entry.setFullUrlElement(new IdType(serverBase, resource.getIdElement().getResourceType(),
				resource.getIdElement().getIdPart(), null));
		return entry;
	}

	public Bundle createHistoryBundle(History history, List<SearchQueryParameterError> errors, UriBuilder bundleUri,
			String format, String pretty)
	{
		Bundle bundle = new Bundle();
		bundle.setTimestamp(new Date());
		bundle.setType(BundleType.HISTORY);
		history.getEntries().stream().map(e -> toBundleEntryComponent(e)).forEach(bundle::addEntry);

		if (!errors.isEmpty())
			bundle.addEntry(toBundleEntryComponent(toOperationOutcomeWarning(errors), SearchEntryMode.OUTCOME));

		bundle.setTotal(history.getTotal());

		setLinks(history.getPageAndCount(), bundleUri, format, pretty, bundle, history.getEntries().isEmpty(),
				history.getTotal());

		return bundle;
	}

	public BundleEntryComponent toBundleEntryComponent(HistoryEntry historyEntry)
	{
		BundleEntryComponent entry = new BundleEntryComponent();
		entry.setFullUrlElement(
				new IdType(serverBase, historyEntry.getResourceType(), historyEntry.getId().toString(), null));
		entry.getRequest().setMethod(HTTPVerb.fromCode(historyEntry.getMethod()))
				.setUrl(historyEntry.getResourceType() + (historyEntry.getResource() == null
						? "/" + historyEntry.getId().toString() + "/_history/" + historyEntry.getVersion()
						: ""));
		entry.setResource(historyEntry.getResource());
		BundleEntryResponseComponent response = entry.getResponse();

		response.setStatus(toStatus(historyEntry.getMethod()));
		response.setLocation(
				toLocation(historyEntry.getResourceType(), historyEntry.getId().toString(), historyEntry.getVersion()));
		response.setEtag(new EntityTag(historyEntry.getVersion(), true).toString());
		response.setLastModified(Date.from(historyEntry.getLastUpdated().atZone(ZoneId.systemDefault()).toInstant()));

		return entry;
	}

	private String toStatus(String method)
	{
		switch (method)
		{
			case "POST":
				return "201 Created";
			case "PUT":
				return "200 OK";
			case "DELETE":
				return "200 OK";
			default:
				throw new RuntimeException("Method " + method + " not supported");
		}
	}

	private String toLocation(String resourceType, String id, String version)
	{
		return new IdType(serverBase, resourceType, id, version).getValue();
	}

	private void setLinks(PageAndCount pageAndCount, UriBuilder bundleUri, String format, String pretty, Bundle bundle,
			boolean isEmpty, int total)
	{
		if (format != null)
			bundleUri = bundleUri.replaceQueryParam("_format", format);
		if (pretty != null)
			bundleUri = bundleUri.replaceQueryParam("_pretty", pretty);

		if (pageAndCount.getCount() > 0)
		{
			bundleUri = bundleUri.replaceQueryParam("_count", pageAndCount.getCount());
			bundleUri = bundleUri.replaceQueryParam("_page", isEmpty ? 1 : pageAndCount.getPage());
		}
		else
			bundleUri = bundleUri.replaceQueryParam("_count", "0");

		bundle.addLink().setRelation("self").setUrlElement(new UriType(bundleUri.build()));

		if (pageAndCount.getCount() > 0 && !isEmpty)
		{
			bundleUri = bundleUri.replaceQueryParam("_page", 1);
			bundleUri = bundleUri.replaceQueryParam("_count", pageAndCount.getCount());
			bundle.addLink().setRelation("first").setUrlElement(new UriType(bundleUri.build()));

			if (pageAndCount.getPage() > 1)
			{
				bundleUri = bundleUri.replaceQueryParam("_page", pageAndCount.getPage() - 1);
				bundleUri = bundleUri.replaceQueryParam("_count", pageAndCount.getCount());
				bundle.addLink().setRelation("previous").setUrlElement(new UriType(bundleUri.build()));
			}
			if (!pageAndCount.isLastPage(total))
			{
				bundleUri = bundleUri.replaceQueryParam("_page", pageAndCount.getPage() + 1);
				bundleUri = bundleUri.replaceQueryParam("_count", pageAndCount.getCount());
				bundle.addLink().setRelation("next").setUrlElement(new UriType(bundleUri.build()));
			}

			bundleUri = bundleUri.replaceQueryParam("_page", pageAndCount.getLastPage(total));
			bundleUri = bundleUri.replaceQueryParam("_count", pageAndCount.getCount());
			bundle.addLink().setRelation("last").setUrlElement(new UriType(bundleUri.build()));
		}
	}

	public OperationOutcome toOperationOutcomeWarning(List<SearchQueryParameterError> errors)
	{
		return toOperationOutcome(errors, IssueSeverity.WARNING);
	}

	public OperationOutcome toOperationOutcomeError(List<SearchQueryParameterError> errors)
	{
		return toOperationOutcome(errors, IssueSeverity.ERROR);
	}

	private OperationOutcome toOperationOutcome(List<SearchQueryParameterError> errors, IssueSeverity severity)
	{
		String diagnostics = errors.stream().map(SearchQueryParameterError::toString).collect(Collectors.joining("; "));
		return createOutcome(severity, IssueType.PROCESSING, diagnostics);
	}

	public Response pathVsElementId(String resourceTypeName, String id, IdType resourceId)
	{
		logger.warn("Path id not equal to {} id ({} vs. {})", resourceTypeName, id, resourceId.getIdPart());

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Path id not equal to " + resourceTypeName + " id (" + id + " vs. " + resourceId.getIdPart() + ")");
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response invalidBaseUrl(String resourceTypeName, IdType resourceId)
	{
		logger.warn("{} id.baseUrl must be null or equal to {}, value {} unexpected", resourceTypeName, serverBase,
				resourceId.getBaseUrl());

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " id.baseUrl must be null or equal to " + serverBase + ", value "
						+ resourceId.getBaseUrl() + " unexpected");
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response badRequest(String queryParameters, List<SearchQueryParameterError> unsupportedQueryParameters)
	{
		String unsupportedQueryParametersString = unsupportedQueryParameters.stream()
				.map(SearchQueryParameterError::toString).collect(Collectors.joining("; "));
		logger.warn("Bad request '{}', unsupported query parameter{} {}", queryParameters,
				unsupportedQueryParameters.size() != 1 ? "s" : "", unsupportedQueryParametersString);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad request '" + queryParameters + "', unsupported query parameter"
						+ (unsupportedQueryParameters.size() != 1 ? "s" : "") + " " + unsupportedQueryParametersString);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response badRequestIdsNotMatching(IdType dbResourceId, IdType resourceId)
	{
		logger.warn("Bad request Id {} does not match db Id {}", resourceId.getValue(), dbResourceId.getValue());

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad request Id " + resourceId.getValue() + " does not match db Id " + dbResourceId.getValue());
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response updateAsCreateNotAllowed(String resourceTypeName, String id)
	{
		logger.warn("{} with id {} not found", resourceTypeName, id);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Resource with id " + id + " not found");
		return Response.status(Status.METHOD_NOT_ALLOWED).entity(outcome).build();
	}

	public Response multipleExists(String resourceTypeName, String ifNoneExistsHeaderValue)
	{
		logger.warn("Multiple {} resources with criteria {} exist", resourceTypeName, ifNoneExistsHeaderValue);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.MULTIPLEMATCHES,
				"Multiple " + resourceTypeName + " resources with criteria '" + ifNoneExistsHeaderValue + "' exist");
		return Response.status(Status.PRECONDITION_FAILED).entity(outcome).build();
	}

	public Response badIfNoneExistHeaderValue(String ifNoneExistsHeaderValue)
	{
		logger.warn("Bad If-None-Exist header value '{}'", ifNoneExistsHeaderValue);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad If-None-Exist header value '" + ifNoneExistsHeaderValue + "'");
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response badIfNoneExistHeaderValue(String ifNoneExistsHeaderValue,
			List<SearchQueryParameterError> unsupportedQueryParameters)
	{
		String unsupportedQueryParametersString = unsupportedQueryParameters.stream()
				.map(SearchQueryParameterError::toString).collect(Collectors.joining("; "));
		logger.warn("Bad If-None-Exist header value '{}', unsupported query parameter{} {}", ifNoneExistsHeaderValue,
				unsupportedQueryParameters.size() != 1 ? "s" : "", unsupportedQueryParametersString);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad If-None-Exist header value '" + ifNoneExistsHeaderValue + "', unsupported query parameter"
						+ (unsupportedQueryParameters.size() != 1 ? "s" : "") + " " + unsupportedQueryParametersString);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response oneExists(Resource resource, String ifNoneExistsHeaderValue)
	{
		logger.info("{} with criteria {} exists", resource.getResourceType().name(), ifNoneExistsHeaderValue);

		OperationOutcome outcome = createOutcome(IssueSeverity.INFORMATION, IssueType.DUPLICATE,
				"Resource with criteria '" + ifNoneExistsHeaderValue + "' exists");

		UriBuilder uri = UriBuilder.fromPath(serverBase);
		URI location = uri.path("/{resourceType}/{id}/" + Constants.PARAM_HISTORY + "/{vid}").build(
				resource.getResourceType().name(), resource.getIdElement().getIdPart(),
				resource.getIdElement().getVersionIdPart());

		return Response.status(Status.OK).entity(outcome).location(location)
				.lastModified(resource.getMeta().getLastUpdated())
				.tag(new EntityTag(resource.getMeta().getVersionId(), true)).build();
	}

	public OperationOutcome unknownReference(Resource resource, ResourceReference resourceReference)
	{
		return unknownReference(resource, resourceReference, null);
	}

	public OperationOutcome unknownReference(Resource resource, ResourceReference resourceReference,
			Integer bundleIndex)
	{
		if (bundleIndex == null)
			logger.warn("Unknown reference at {} in resource of type {} with id {}", resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId());
		else
			logger.warn("Unknown reference at {} in resource of type {} with id {} at bundle index {}",
					resourceReference.getLocation(), resource.getResourceType().name(), resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Unknown reference at " + resourceReference.getLocation() + " in resource of type "
						+ resource.getResourceType().name() + " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex));
	}

	public OperationOutcome referenceTargetTypeNotSupportedByImplementation(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference)
	{
		if (bundleIndex == null)
			logger.warn(
					"Reference target type of reference at {} in resource of type {} with id {} not supported by this implementation",
					resourceReference.getLocation(), resource.getResourceType().name(), resource.getId());
		else
			logger.warn(
					"Reference target type of reference at {} in resource of type {} with id {} at bundle index {} not supported by this implementation",
					resourceReference.getLocation(), resource.getResourceType().name(), resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Reference target type of reference at " + resourceReference.getLocation() + " in resource of type "
						+ resource.getResourceType().name() + " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex)
						+ " not supported by this implementation");
	}

	public OperationOutcome referenceTargetTypeNotSupportedByResource(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference)
	{
		if (bundleIndex == null)
			logger.warn("Reference target type of reference at {} in resource of type {} with id {} not supported",
					resourceReference.getLocation(), resource.getResourceType().name(), resource.getId());
		else
			logger.warn(
					"Reference target type of reference at {} in resource of type {} with id {} at bundle index {} not supported",
					resourceReference.getLocation(), resource.getResourceType().name(), resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Reference target type of reference at " + resourceReference.getLocation() + " in resource of type "
						+ resource.getResourceType().name() + " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not supported");
	}

	public OperationOutcome referenceTargetNotFoundLocally(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference)
	{
		if (bundleIndex == null)
			logger.warn("Reference target {} of reference at {} in resource of type {} with id {} not found",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId());
		else
			logger.warn(
					"Reference target {} of reference at {} in resource of type {} with id {} at bundle index {} not found",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Reference target " + resourceReference.getValue() + " of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found");
	}

	public OperationOutcome referenceTargetNotFoundRemote(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference, String serverBase)
	{
		if (bundleIndex == null)
			logger.warn(
					"Reference target {} of reference at {} in resource of type {} with id {} not found on server {}",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), serverBase);
		else
			logger.warn(
					"Reference target {} of reference at {} in resource of type {} with id {} at bundle index {} not found on server {}",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), bundleIndex, serverBase);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Reference target " + resourceReference.getValue() + " of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found on server "
						+ serverBase);
	}

	public OperationOutcome referenceTargetCouldNotBeResolvedOnRemote(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference, String serverBase)
	{
		if (bundleIndex == null)
			logger.warn(
					"Reference target {} of reference at {} in resource of type {} with id {} could not be resolved on server {} (reason hidden)",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), serverBase);
		else
			logger.warn(
					"Reference target {} of reference at {} in resource of type {} with id {} at bundle index {} could not be resolved on server {} (reason hidden)",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), bundleIndex, serverBase);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Reference target " + resourceReference.getValue() + " of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex)
						+ " could not be resolved on server " + serverBase + " (reason hidden)");
	}

	public OperationOutcome noEndpointFoundForLiteralExternalReference(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference)
	{
		if (bundleIndex == null)
			logger.warn(
					"No Endpoint found for reference target {} of reference at {} in resource of type {} with id {}",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId());
		else
			logger.warn(
					"No Endpoint found for reference target {} of reference at {} in resource of type {} with id {} at bundle index {} not found",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"No Endpoint found for reference target " + resourceReference.getValue() + " of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found");
	}

	public OperationOutcome badReference(boolean logicalNotConditional, Integer bundleIndex, Resource resource,
			ResourceReference resourceReference, String queryParameters,
			List<SearchQueryParameterError> unsupportedQueryParameters)
	{
		String unsupportedQueryParametersString = unsupportedQueryParameters.stream()
				.map(SearchQueryParameterError::toString).collect(Collectors.joining("; "));

		if (bundleIndex == null)
			logger.warn(
					"{} reference {} at {} in resource of type {} with id {} contains unsupported queryparameter{} {}",
					logicalNotConditional ? "Logical" : "Conditional", queryParameters, resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId(),
					unsupportedQueryParameters.size() != 1 ? "s" : "", unsupportedQueryParametersString);
		else
			logger.warn(
					"{} reference {} at {} in resource of type {} with id {} at bundle index {} contains unsupported queryparameter{} {}",
					logicalNotConditional ? "Logical" : "Conditional", queryParameters, resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId(), bundleIndex,
					unsupportedQueryParameters.size() != 1 ? "s" : "", unsupportedQueryParametersString);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				(logicalNotConditional ? "Logical" : "Conditional") + " reference " + queryParameters + " at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex)
						+ " contains unsupported queryparameter" + (unsupportedQueryParameters.size() != 1 ? "s" : "")
						+ " " + unsupportedQueryParametersString);
	}

	public OperationOutcome referenceTargetNotFoundLocallyByIdentifier(Resource resource,
			ResourceReference resourceReference)
	{
		return referenceTargetNotFoundLocallyByIdentifier(null, resource, resourceReference);
	}

	public OperationOutcome referenceTargetNotFoundLocallyByIdentifier(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference)
	{
		if (bundleIndex == null)
			logger.warn(
					"Reference target by identifier '{}|{}' of reference at {} in resource of type {} with id {} not found",
					resourceReference.getReference().getIdentifier().getSystem(),
					resourceReference.getReference().getIdentifier().getValue(), resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId());
		else
			logger.warn(
					"Reference target by identifier '{}|{}' of reference at {} in resource of type {} with id {} at bundle index {} not found",
					resourceReference.getReference().getIdentifier().getSystem(),
					resourceReference.getReference().getIdentifier().getValue(), resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Reference target by identifier '" + resourceReference.getReference().getIdentifier().getSystem() + "|"
						+ resourceReference.getReference().getIdentifier().getValue() + "' of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found");
	}

	public OperationOutcome referenceTargetMultipleMatchesLocallyByIdentifier(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference, int overallCount)
	{
		if (bundleIndex == null)
			logger.warn(
					"Found {} matches for reference target by identifier '{}|{}' of reference at {} in resource of type {} with id {}",
					overallCount, resourceReference.getReference().getIdentifier().getSystem(),
					resourceReference.getReference().getIdentifier().getValue(), resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId());
		else
			logger.warn(
					"Found {} matches for reference target by identifier '{}|{}' of reference at {} in resource of type {} with id {} at bundle index {}",
					overallCount, resourceReference.getReference().getIdentifier().getSystem(),
					resourceReference.getReference().getIdentifier().getValue(), resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Found " + overallCount + " matches for reference target by identifier '"
						+ resourceReference.getReference().getIdentifier().getSystem() + "|"
						+ resourceReference.getReference().getIdentifier().getValue() + "' of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found");
	}

	public OperationOutcome referenceTargetNotFoundLocallyByCondition(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference)
	{
		if (bundleIndex == null)
			logger.warn(
					"Reference target by condition '{}' of reference at {} in resource of type {} with id {} not found",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId());
		else
			logger.warn(
					"Reference target by condition '{}' of reference at {} in resource of type {} with id {} at bundle index {} not found",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Reference target by condition '" + resourceReference.getValue() + "' of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found");
	}

	public OperationOutcome referenceTargetMultipleMatchesLocallyByCondition(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference, int overallCount)
	{
		if (bundleIndex == null)
			logger.warn(
					"Found {} matches for reference target by condition '{}' of reference at {} in resource of type {} with id {}",
					overallCount, resourceReference.getValue(), resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId());
		else
			logger.warn(
					"Found {} matches for reference target by condition '{}' of reference at {} in resource of type {} with id {} at bundle index {}",
					overallCount, resourceReference.getValue(), resourceReference.getLocation(),
					resource.getResourceType().name(), resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Found " + overallCount + " matches for reference target by condition '" + resourceReference.getValue()
						+ "' of reference at " + resourceReference.getLocation() + " in resource of type "
						+ resource.getResourceType().name() + " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found");
	}

	public OperationOutcome referenceTargetBadCondition(Integer bundleIndex, Resource resource,
			ResourceReference resourceReference)
	{
		if (bundleIndex == null)
			logger.warn("Bad conditional reference target '{}' of reference at {} in resource of type {} with id {}",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId());
		else
			logger.warn(
					"Bad conditional reference target '{}' of reference at {} in resource of type {} with id {} at bundle index {}",
					resourceReference.getValue(), resourceReference.getLocation(), resource.getResourceType().name(),
					resource.getId(), bundleIndex);

		return createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad conditional reference target '" + resourceReference.getValue() + "' of reference at "
						+ resourceReference.getLocation() + " in resource of type " + resource.getResourceType().name()
						+ " with id " + resource.getId()
						+ (bundleIndex == null ? "" : " at bundle index " + bundleIndex) + " not found");
	}

	public Response badDeleteRequestUrl(int bundleIndex, String url)
	{
		logger.warn("Bad delete request url {} at bundle index {}", url, bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad delete request url " + url + " at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response badCreateRequestUrl(int bundleIndex, String url)
	{
		logger.warn("Bad crate request url {} at bundle index {}", url, bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad crete request url " + url + " at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response badUpdateRequestUrl(int bundleIndex, String url)
	{
		logger.warn("Bad update request url {} at bundle index {}", url, bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad update request url " + url + " at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response badReadRequestUrl(int bundleIndex, String url)
	{
		logger.warn("Bad read request url {} at bundle index {}", url, bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad read request url " + url + " at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response resourceTypeNotSupportedByImplementation(int bundleIndex, String resourceTypeName)
	{
		logger.warn("Resource type {} at bundle index {} not supported by this implementation", resourceTypeName,
				bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING, "Resource type "
				+ resourceTypeName + " at bundle index " + bundleIndex + " not supported by this implementation");
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response badConditionalDeleteRequest(int bundleIndex, String queryParameters,
			List<SearchQueryParameterError> unsupportedQueryParameters)
	{
		String unsupportedQueryParametersString = unsupportedQueryParameters.stream()
				.map(SearchQueryParameterError::toString).collect(Collectors.joining("; "));
		logger.warn("Bad conditional delete request '{}', unsupported query parameter{} {} at bundle index {}",
				queryParameters, unsupportedQueryParameters.size() != 1 ? "s" : "", unsupportedQueryParametersString,
				bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad conditional delete request '" + queryParameters + "', unsupported query parameter"
						+ (unsupportedQueryParameters.size() != 1 ? "s" : "") + " " + unsupportedQueryParametersString
						+ " at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response badConditionalDeleteRequestMultipleMatches(int bundleIndex, String resourceTypeName,
			String queryParameters)
	{
		logger.warn("Multiple {} resources with criteria '{}' exist for delete request at bundle index {}",
				resourceTypeName, queryParameters, bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.MULTIPLEMATCHES,
				"Multiple " + resourceTypeName + " resources with criteria '" + queryParameters
						+ "' exist for delete request at bundle index " + bundleIndex);
		return Response.status(Status.PRECONDITION_FAILED).entity(outcome).build();
	}

	public Response badBundleRequest(String message)
	{
		logger.warn("Bad bundle request - {}", message);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad bundle request - " + message);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	public Response pathVsElementIdInBundle(int bundleIndex, String resourceTypeName, String id, IdType resourceId)
	{
		logger.warn("Path id not equal to {} id ({} vs. {}) at bundle index {}", resourceTypeName, id,
				resourceId.getIdPart(), bundleIndex);

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Path id not equal to " + resourceTypeName + " id (" + id + " vs. " + resourceId.getIdPart()
						+ ") at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response invalidBaseUrlInBundle(int bundleIndex, String resourceTypeName, IdType resourceId)
	{
		logger.warn("{} id.baseUrl must be null or equal to {}, value {} unexpected at bundle index {}",
				resourceTypeName, serverBase, resourceId.getBaseUrl(), bundleIndex);

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " id.baseUrl must be null or equal to " + serverBase + ", value "
						+ resourceId.getBaseUrl() + " unexpected at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response nonMatchingResourceTypeAndRequestUrlInBundle(int bundleIndex, String resourceTypeName, String url)
	{
		logger.warn("Non matching resource type {} and request url {} at bundle index {}", resourceTypeName, url,
				bundleIndex);

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING, "Non matching resource type "
				+ resourceTypeName + " and request url " + url + " at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response unsupportedConditionalUpdateQuery(int bundleIndex, String query,
			List<SearchQueryParameterError> unsupportedQueryParameters)
	{
		String unsupportedQueryParametersString = unsupportedQueryParameters.stream()
				.map(SearchQueryParameterError::toString).collect(Collectors.joining("; "));

		logger.warn("Bad conditional update request '{}', unsupported query parameter{} {} at bundle index {}", query,
				unsupportedQueryParameters.size() != 1 ? "s" : "", unsupportedQueryParametersString, bundleIndex);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad conditional update request '" + query + "', unsupported query parameter"
						+ (unsupportedQueryParameters.size() != 1 ? "s" : "") + " " + unsupportedQueryParametersString
						+ " at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(outcome).build();

	}

	public Response bundleEntryResouceMissingId(int bundleIndex, String resourceTypeName)
	{
		logger.warn("Bundle entry of type {} at bundle index {} is missing id value", resourceTypeName, bundleIndex);

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING, "Bundle entry of type "
				+ resourceTypeName + " at bundle index " + bundleIndex + " is missing id value");
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response badBundleEntryFullUrl(int bundleIndex, String fullUrl)
	{
		logger.warn("Bad entry fullUrl '{}' at bundle index {}", fullUrl, bundleIndex);

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bad entry fullUrl '" + fullUrl + "' at bundle index " + bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response bundleEntryBadResourceId(int bundleIndex, String resourceTypeName, String urlUuidPrefix)
	{
		logger.warn("Bundle entry of type {} at bundle index {} id value not starting with {}", resourceTypeName,
				bundleIndex, urlUuidPrefix);

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Bundle entry of type " + resourceTypeName + " at bundle index " + bundleIndex
						+ " id value not starting with " + urlUuidPrefix);
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response badBundleEntryFullUrlVsResourceId(int bundleIndex, String fullUrl, String idValue)
	{
		logger.warn("Resource id not equal to entry fullUrl ({} vs. {}) at bundle index {}", idValue, fullUrl,
				bundleIndex);

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				"Resource id not equal to entry fullUrl (" + idValue + " vs. " + fullUrl + ") at bundle index "
						+ bundleIndex);
		return Response.status(Status.BAD_REQUEST).entity(out).build();
	}

	public Response forbiddenNotAllowed(String operation, User user)
	{
		logger.warn("Operation {} forbidden for user '{}'", operation, user.getName());

		OperationOutcome out = createOutcome(IssueSeverity.ERROR, IssueType.FORBIDDEN,
				"Operation " + operation + " forbidden");
		return Response.status(Status.FORBIDDEN).entity(out).build();
	}

	public Response notFound(String id, String resourceTypeName)
	{
		logger.warn("{} with id {} not found", resourceTypeName, id);

		OperationOutcome outcome = createOutcome(IssueSeverity.ERROR, IssueType.PROCESSING,
				resourceTypeName + " with id " + id + " not found");
		return Response.status(Status.NOT_FOUND).entity(outcome).build();
	}

	public Response forbiddenNotValid(String operation, User user, String resourceType,
			ValidationResult validationResult)
	{
		OperationOutcome outcome = new OperationOutcome();
		validationResult.populateOperationOutcome(outcome);

		logger.warn("Operation {} forbidden, {} resource not valid for user '{}'", operation, resourceType,
				user.getName());

		return Response.status(Status.FORBIDDEN).entity(outcome).build();
	}

	public Response unableToGenerateSnapshot(StructureDefinition resource, Integer bundleIndex,
			List<ValidationMessage> messages)
	{
		if (bundleIndex == null)
			logger.warn(
					"Unable to generate StructureDefinition snapshot for profile with url {}, version {} and id {}: {}",
					resource.getUrl(), resource.getVersion(), resource.getId());
		else
			logger.warn(
					"Unable to generate StructureDefinition snapshot for profile with url {}, version {} and id {} at bundle index {}: {}",
					resource.getUrl(), resource.getVersion(), resource.getId(), bundleIndex);

		OperationOutcome outcome = new OperationOutcome();

		messages.forEach(m -> outcome.addIssue().setSeverity(convert(m.getLevel())).setCode(convert(m.getType()))
				.setDiagnostics(m.summary()));

		return Response.status(Status.BAD_REQUEST).entity(outcome).build();
	}

	private IssueSeverity convert(org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity severity)
	{
		switch (severity)
		{
			case FATAL:
				return IssueSeverity.FATAL;
			case ERROR:
				return IssueSeverity.ERROR;
			case WARNING:
				return IssueSeverity.WARNING;
			case INFORMATION:
				return IssueSeverity.INFORMATION;
			case NULL:
				return IssueSeverity.NULL;
			default:
				throw new RuntimeException("IssueSeverity " + severity + " not supported");
		}
	}

	private IssueType convert(org.hl7.fhir.utilities.validation.ValidationMessage.IssueType type)
	{
		switch (type)
		{
			case INVALID:
				return IssueType.INVALID;
			case STRUCTURE:
				return IssueType.STRUCTURE;
			case REQUIRED:
				return IssueType.REQUIRED;
			case VALUE:
				return IssueType.VALUE;
			case INVARIANT:
				return IssueType.INVARIANT;
			case SECURITY:
				return IssueType.SECURITY;
			case LOGIN:
				return IssueType.LOGIN;
			case UNKNOWN:
				return IssueType.UNKNOWN;
			case EXPIRED:
				return IssueType.EXPIRED;
			case FORBIDDEN:
				return IssueType.FORBIDDEN;
			case SUPPRESSED:
				return IssueType.SUPPRESSED;
			case PROCESSING:
				return IssueType.PROCESSING;
			case NOTSUPPORTED:
				return IssueType.NOTSUPPORTED;
			case DUPLICATE:
				return IssueType.DUPLICATE;
			case MULTIPLEMATCHES:
				return IssueType.MULTIPLEMATCHES;
			case NOTFOUND:
				return IssueType.NOTFOUND;
			case DELETED:
				return IssueType.DELETED;
			case TOOLONG:
				return IssueType.TOOLONG;
			case CODEINVALID:
				return IssueType.CODEINVALID;
			case EXTENSION:
				return IssueType.EXTENSION;
			case TOOCOSTLY:
				return IssueType.TOOCOSTLY;
			case BUSINESSRULE:
				return IssueType.BUSINESSRULE;
			case CONFLICT:
				return IssueType.CONFLICT;
			case TRANSIENT:
				return IssueType.TRANSIENT;
			case LOCKERROR:
				return IssueType.LOCKERROR;
			case NOSTORE:
				return IssueType.NOSTORE;
			case EXCEPTION:
				return IssueType.EXCEPTION;
			case TIMEOUT:
				return IssueType.TIMEOUT;
			case INCOMPLETE:
				return IssueType.INCOMPLETE;
			case THROTTLED:
				return IssueType.THROTTLED;
			case INFORMATIONAL:
				return IssueType.INFORMATIONAL;
			case NULL:
				return IssueType.NULL;

			default:
				throw new RuntimeException("IssueType " + type + " not supported");
		}
	}
}
