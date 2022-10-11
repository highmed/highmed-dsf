package org.highmed.fhir.client;

import java.io.InputStream;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.highmed.dsf.fhir.adapter.AbstractFhirAdapter;
import org.highmed.dsf.fhir.adapter.ActivityDefinitionJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.ActivityDefinitionXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.BinaryJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.BinaryXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.BundleJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.BundleXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.CapabilityStatementJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.CapabilityStatementXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.CodeSystemJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.CodeSystemXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.DocumentReferenceJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.DocumentReferenceXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.EndpointJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.EndpointXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.GroupJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.GroupXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.HealthcareServiceJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.HealthcareServiceXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.LibraryJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.LibraryXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.LocationJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.LocationXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.MeasureJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.MeasureReportJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.MeasureReportXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.MeasureXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.NamingSystemJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.NamingSystemXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.OperationOutcomeJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.OperationOutcomeXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.OrganizationAffiliationJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.OrganizationAffiliationXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.OrganizationJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.OrganizationXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.ParametersJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.ParametersXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.PatientJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.PatientXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.PractitionerJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.PractitionerRoleJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.PractitionerRoleXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.PractitionerXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.ProvenanceJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.ProvenanceXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.QuestionnaireJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.QuestionnaireResponseJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.QuestionnaireResponseXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.QuestionnaireXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.ResearchStudyJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.ResearchStudyXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.StructureDefinitionJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.StructureDefinitionXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.SubscriptionJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.SubscriptionXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.TaskJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.TaskXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.ValueSetJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.ValueSetXmlFhirAdapter;
import org.highmed.dsf.fhir.prefer.PreferHandlingType;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

public class FhirWebserviceClientJersey extends AbstractJerseyClient implements FhirWebserviceClient
{
	private static final String RFC_7231_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

	private static final Logger logger = LoggerFactory.getLogger(FhirWebserviceClientJersey.class);

	private final ReferenceCleaner referenceCleaner;
	private final Map<String, Class<?>> resourceTypeByNames = new HashMap<>();

	private final PreferReturnMinimalWithRetry preferReturnMinimal;
	private final PreferReturnOutcomeWithRetry preferReturnOutcome;

	public FhirWebserviceClientJersey(String baseUrl, KeyStore trustStore, KeyStore keyStore, char[] keyStorePassword,
			String proxySchemeHostPort, String proxyUserName, char[] proxyPassword, int connectTimeout, int readTimeout,
			boolean logRequests, ObjectMapper objectMapper, FhirContext fhirContext, ReferenceCleaner referenceCleaner)
	{
		super(baseUrl, trustStore, keyStore, keyStorePassword, proxySchemeHostPort, proxyUserName, proxyPassword,
				connectTimeout, readTimeout, objectMapper, components(fhirContext), logRequests);

		this.referenceCleaner = referenceCleaner;

		registeredComponents.stream().filter(e -> e instanceof AbstractFhirAdapter).map(e -> (AbstractFhirAdapter<?>) e)
				.forEach(a -> resourceTypeByNames.put(a.getResourceTypeName(), a.getResourceType()));

		preferReturnMinimal = new PreferReturnMinimalWithRetryImpl(this);
		preferReturnOutcome = new PreferReturnOutcomeWithRetryImpl(this);
	}

	public static List<AbstractFhirAdapter<?>> components(FhirContext fhirContext)
	{
		return Arrays.asList(new ActivityDefinitionJsonFhirAdapter(fhirContext),
				new ActivityDefinitionXmlFhirAdapter(fhirContext), new BinaryJsonFhirAdapter(fhirContext),
				new BinaryXmlFhirAdapter(fhirContext), new BundleJsonFhirAdapter(fhirContext),
				new BundleXmlFhirAdapter(fhirContext), new CapabilityStatementJsonFhirAdapter(fhirContext),
				new CapabilityStatementXmlFhirAdapter(fhirContext), new CodeSystemJsonFhirAdapter(fhirContext),
				new CodeSystemXmlFhirAdapter(fhirContext), new DocumentReferenceJsonFhirAdapter(fhirContext),
				new DocumentReferenceXmlFhirAdapter(fhirContext), new GroupJsonFhirAdapter(fhirContext),
				new GroupXmlFhirAdapter(fhirContext), new EndpointJsonFhirAdapter(fhirContext),
				new EndpointXmlFhirAdapter(fhirContext), new HealthcareServiceJsonFhirAdapter(fhirContext),
				new HealthcareServiceXmlFhirAdapter(fhirContext), new LibraryJsonFhirAdapter(fhirContext),
				new LibraryXmlFhirAdapter(fhirContext), new LocationJsonFhirAdapter(fhirContext),
				new LocationXmlFhirAdapter(fhirContext), new MeasureJsonFhirAdapter(fhirContext),
				new MeasureXmlFhirAdapter(fhirContext), new MeasureReportJsonFhirAdapter(fhirContext),
				new MeasureReportXmlFhirAdapter(fhirContext), new NamingSystemJsonFhirAdapter(fhirContext),
				new NamingSystemXmlFhirAdapter(fhirContext), new OperationOutcomeJsonFhirAdapter(fhirContext),
				new OperationOutcomeXmlFhirAdapter(fhirContext), new OrganizationJsonFhirAdapter(fhirContext),
				new OrganizationXmlFhirAdapter(fhirContext), new OrganizationAffiliationJsonFhirAdapter(fhirContext),
				new OrganizationAffiliationXmlFhirAdapter(fhirContext), new ParametersJsonFhirAdapter(fhirContext),
				new ParametersXmlFhirAdapter(fhirContext), new PatientJsonFhirAdapter(fhirContext),
				new PatientXmlFhirAdapter(fhirContext), new PractitionerJsonFhirAdapter(fhirContext),
				new PractitionerXmlFhirAdapter(fhirContext), new PractitionerRoleJsonFhirAdapter(fhirContext),
				new PractitionerRoleXmlFhirAdapter(fhirContext), new ProvenanceJsonFhirAdapter(fhirContext),
				new ProvenanceXmlFhirAdapter(fhirContext), new QuestionnaireXmlFhirAdapter(fhirContext),
				new QuestionnaireJsonFhirAdapter(fhirContext), new QuestionnaireResponseXmlFhirAdapter(fhirContext),
				new QuestionnaireResponseJsonFhirAdapter(fhirContext), new ResearchStudyJsonFhirAdapter(fhirContext),
				new ResearchStudyXmlFhirAdapter(fhirContext), new StructureDefinitionJsonFhirAdapter(fhirContext),
				new StructureDefinitionXmlFhirAdapter(fhirContext), new SubscriptionJsonFhirAdapter(fhirContext),
				new SubscriptionXmlFhirAdapter(fhirContext), new TaskJsonFhirAdapter(fhirContext),
				new TaskXmlFhirAdapter(fhirContext), new ValueSetJsonFhirAdapter(fhirContext),
				new ValueSetXmlFhirAdapter(fhirContext));
	}

	private WebApplicationException handleError(Response response)
	{
		try
		{
			OperationOutcome outcome = response.readEntity(OperationOutcome.class);
			String message = toString(outcome);

			logger.warn("OperationOutcome: {}", message);
			return new WebApplicationException(message, response.getStatus());
		}
		catch (ProcessingException e)
		{
			response.close();
			logger.warn("{}: {}", e.getClass().getName(), e.getMessage());
			return new WebApplicationException(e, response.getStatus());
		}
	}

	private String toString(OperationOutcome outcome)
	{
		return outcome == null ? ""
				: outcome.getIssue().stream().map(i -> toString(i)).collect(Collectors.joining("\n"));
	}

	private String toString(OperationOutcomeIssueComponent issue)
	{
		return issue == null ? "" : issue.getSeverity() + " " + issue.getCode() + " " + issue.getDiagnostics();
	}

	private void logStatusAndHeaders(Response response)
	{
		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header Location: {}", response.getLocation());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));
	}

	private PreferReturn toPreferReturn(PreferReturnType returnType, Class<? extends Resource> resourceType,
			Response response)
	{
		switch (returnType)
		{
			case REPRESENTATION:
				// TODO remove workaround if HAPI bug fixed
				Resource resource = referenceCleaner.cleanReferenceResourcesIfBundle(response.readEntity(resourceType));
				return PreferReturn.resource(resource);
			case MINIMAL:
				return PreferReturn.minimal(response.getLocation());
			case OPERATION_OUTCOME:
				return PreferReturn.outcome(response.readEntity(OperationOutcome.class));
			default:
				throw new RuntimeException(PreferReturn.class.getName() + " value " + returnType + " not supported");
		}
	}

	@Override
	public PreferReturnMinimalWithRetry withMinimalReturn()
	{
		return preferReturnMinimal;
	}

	@Override
	public PreferReturnOutcomeWithRetry withOperationOutcomeReturn()
	{
		return preferReturnOutcome;
	}

	PreferReturn create(PreferReturnType returnType, Resource resource)
	{
		Objects.requireNonNull(returnType, "returnType");
		Objects.requireNonNull(resource, "resource");

		Response response = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name()).request()
				.header(Constants.HEADER_PREFER, returnType.getHeaderValue()).accept(Constants.CT_FHIR_JSON_NEW)
				.post(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logStatusAndHeaders(response);

		if (Status.CREATED.getStatusCode() == response.getStatus())
			return toPreferReturn(returnType, resource.getClass(), response);
		else
			throw handleError(response);
	}

	PreferReturn createConditionaly(PreferReturnType returnType, Resource resource, String ifNoneExistCriteria)
	{
		Objects.requireNonNull(returnType, "returnType");
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(ifNoneExistCriteria, "ifNoneExistCriteria");

		Response response = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name()).request()
				.header(Constants.HEADER_PREFER, returnType.getHeaderValue())
				.header(Constants.HEADER_IF_NONE_EXIST, ifNoneExistCriteria).accept(Constants.CT_FHIR_JSON_NEW)
				.post(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logStatusAndHeaders(response);

		if (Status.CREATED.getStatusCode() == response.getStatus())
			return toPreferReturn(returnType, resource.getClass(), response);
		else
			throw handleError(response);
	}

	PreferReturn createBinary(PreferReturnType returnType, InputStream in, MediaType mediaType,
			String securityContextReference)
	{
		Objects.requireNonNull(returnType, "returnType");
		Objects.requireNonNull(in, "in");
		Objects.requireNonNull(mediaType, "mediaType");
		// securityContextReference may be null

		Builder request = getResource().path("Binary").request().header(Constants.HEADER_PREFER,
				returnType.getHeaderValue());
		if (securityContextReference != null && !securityContextReference.isBlank())
			request = request.header(Constants.HEADER_X_SECURITY_CONTEXT, securityContextReference);
		Response response = request.accept(Constants.CT_FHIR_JSON_NEW).post(Entity.entity(in, mediaType));

		logStatusAndHeaders(response);

		if (Status.CREATED.getStatusCode() == response.getStatus())
			return toPreferReturn(returnType, Binary.class, response);
		else
			throw handleError(response);
	}

	PreferReturn update(PreferReturnType returnType, Resource resource)
	{
		Objects.requireNonNull(returnType, "returnType");
		Objects.requireNonNull(resource, "resource");

		Builder builder = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name())
				.path(resource.getIdElement().getIdPart()).request()
				.header(Constants.HEADER_PREFER, returnType.getHeaderValue()).accept(Constants.CT_FHIR_JSON_NEW);

		if (resource.getMeta().hasVersionId())
			builder.header(Constants.HEADER_IF_MATCH, new EntityTag(resource.getMeta().getVersionId(), true));

		Response response = builder.put(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logStatusAndHeaders(response);

		if (Status.OK.getStatusCode() == response.getStatus())
			return toPreferReturn(returnType, resource.getClass(), response);
		else
			throw handleError(response);
	}

	PreferReturn updateConditionaly(PreferReturnType returnType, Resource resource, Map<String, List<String>> criteria)
	{
		Objects.requireNonNull(returnType, "returnType");
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(criteria, "criteria");
		if (criteria.isEmpty())
			throw new IllegalArgumentException("criteria map empty");

		WebTarget target = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name());

		for (Entry<String, List<String>> entry : criteria.entrySet())
			target = target.queryParam(entry.getKey(), entry.getValue().toArray());

		Builder builder = target.request().accept(Constants.CT_FHIR_JSON_NEW).header(Constants.HEADER_PREFER,
				returnType.getHeaderValue());

		if (resource.getMeta().hasVersionId())
			builder.header(Constants.HEADER_IF_MATCH, new EntityTag(resource.getMeta().getVersionId(), true));

		Response response = builder.put(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logStatusAndHeaders(response);

		if (Status.CREATED.getStatusCode() == response.getStatus() || Status.OK.getStatusCode() == response.getStatus())
			return toPreferReturn(returnType, resource.getClass(), response);
		else
			throw handleError(response);
	}

	PreferReturn updateBinary(PreferReturnType returnType, String id, InputStream in, MediaType mediaType,
			String securityContextReference)
	{
		Objects.requireNonNull(returnType, "returnType");
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(in, "in");
		Objects.requireNonNull(mediaType, "mediaType");
		// securityContextReference may be null

		Builder request = getResource().path("Binary").path(id).request().header(Constants.HEADER_PREFER,
				returnType.getHeaderValue());
		if (securityContextReference != null && !securityContextReference.isBlank())
			request = request.header(Constants.HEADER_X_SECURITY_CONTEXT, securityContextReference);
		Response response = request.accept(Constants.CT_FHIR_JSON_NEW).put(Entity.entity(in, mediaType));

		logStatusAndHeaders(response);

		if (Status.CREATED.getStatusCode() == response.getStatus())
			return toPreferReturn(returnType, Binary.class, response);
		else
			throw handleError(response);
	}

	Bundle postBundle(PreferReturnType returnType, Bundle bundle)
	{
		Objects.requireNonNull(bundle, "bundle");

		Response response = getResource().request().header(Constants.HEADER_PREFER, returnType.getHeaderValue())
				.accept(Constants.CT_FHIR_JSON_NEW).post(Entity.entity(bundle, Constants.CT_FHIR_JSON_NEW));

		logStatusAndHeaders(response);

		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return referenceCleaner.cleanReferenceResourcesIfBundle(response.readEntity(Bundle.class));
		else
			throw handleError(response);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R create(R resource)
	{
		return (R) create(PreferReturnType.REPRESENTATION, resource).getResource();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R createConditionaly(R resource, String ifNoneExistCriteria)
	{
		return (R) createConditionaly(PreferReturnType.REPRESENTATION, resource, ifNoneExistCriteria).getResource();
	}

	@Override
	public Binary createBinary(InputStream in, MediaType mediaType, String securityContextReference)
	{
		return (Binary) createBinary(PreferReturnType.REPRESENTATION, in, mediaType, securityContextReference)
				.getResource();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R update(R resource)
	{
		return (R) update(PreferReturnType.REPRESENTATION, resource).getResource();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R updateConditionaly(R resource, Map<String, List<String>> criteria)
	{
		return (R) updateConditionaly(PreferReturnType.REPRESENTATION, resource, criteria).getResource();
	}

	@Override
	public Binary updateBinary(String id, InputStream in, MediaType mediaType, String securityContextReference)
	{
		return (Binary) updateBinary(PreferReturnType.REPRESENTATION, id, in, mediaType, securityContextReference)
				.getResource();
	}

	@Override
	public Bundle postBundle(Bundle bundle)
	{
		return postBundle(PreferReturnType.REPRESENTATION, bundle);
	}

	@Override
	public void delete(Class<? extends Resource> resourceClass, String id)
	{
		Objects.requireNonNull(resourceClass, "resourceClass");
		Objects.requireNonNull(id, "id");

		Response response = getResource().path(resourceClass.getAnnotation(ResourceDef.class).name()).path(id).request()
				.accept(Constants.CT_FHIR_JSON_NEW).delete();

		logStatusAndHeaders(response);

		if (Status.OK.getStatusCode() != response.getStatus()
				&& Status.NO_CONTENT.getStatusCode() != response.getStatus())
			throw handleError(response);
		else
			response.close();
	}

	@Override
	public void deleteConditionaly(Class<? extends Resource> resourceClass, Map<String, List<String>> criteria)
	{
		Objects.requireNonNull(resourceClass, "resourceClass");
		Objects.requireNonNull(criteria, "criteria");
		if (criteria.isEmpty())
			throw new IllegalArgumentException("criteria map empty");

		WebTarget target = getResource().path(resourceClass.getAnnotation(ResourceDef.class).name());

		for (Entry<String, List<String>> entry : criteria.entrySet())
			target = target.queryParam(entry.getKey(), entry.getValue().toArray());

		Response response = target.request().accept(Constants.CT_FHIR_JSON_NEW).delete();

		logStatusAndHeaders(response);

		if (Status.OK.getStatusCode() != response.getStatus()
				&& Status.NO_CONTENT.getStatusCode() != response.getStatus())
			throw handleError(response);
		else
			response.close();
	}

	@Override
	public void deletePermanently(Class<? extends Resource> resourceClass, String id)
	{
		Objects.requireNonNull(resourceClass, "resourceClass");
		Objects.requireNonNull(id, "id");

		Response response = getResource().path(resourceClass.getAnnotation(ResourceDef.class).name()).path(id)
				.path("$permanent-delete").request().accept(Constants.CT_FHIR_JSON_NEW).post(null);

		logStatusAndHeaders(response);

		if (Status.OK.getStatusCode() != response.getStatus())
			throw handleError(response);
		else
			response.close();
	}

	@Override
	public Resource read(String resourceTypeName, String id)
	{
		Objects.requireNonNull(resourceTypeName, "resourceTypeName");
		Objects.requireNonNull(id, "id");
		if (!resourceTypeByNames.containsKey(resourceTypeName))
			throw new IllegalArgumentException("Resource of type " + resourceTypeName + " not supported");

		Response response = getResource().path(resourceTypeName).path(id).request().accept(Constants.CT_FHIR_JSON_NEW)
				.get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return referenceCleaner.cleanReferenceResourcesIfBundle(
					(Resource) response.readEntity(resourceTypeByNames.get(resourceTypeName)));
		else
			throw handleError(response);
	}

	@Override
	public <R extends Resource> R read(Class<R> resourceType, String id)
	{
		return read(resourceType, id, (R) null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R read(R oldValue)
	{
		return read((Class<R>) oldValue.getClass(), oldValue.getIdElement().getIdPart(), oldValue);
	}

	private <R extends Resource> R read(Class<R> resourceType, String id, R oldValue)
	{
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(id, "id");

		Builder request = getResource().path(resourceType.getAnnotation(ResourceDef.class).name()).path(id).request();

		if (oldValue != null && oldValue.hasMeta())
		{
			if (oldValue.getMeta().hasVersionId())
			{
				String eTag = new EntityTag(oldValue.getMeta().getVersionIdElement().getValue(), true).toString();
				request.header(HttpHeaders.IF_NONE_MATCH, eTag);
				logger.trace("Sending {} Header with value '{}'", HttpHeaders.IF_NONE_MATCH, eTag.toString());
			}

			if (oldValue.getMeta().hasLastUpdated())
			{
				String dateValue = formatRfc7231(oldValue.getMeta().getLastUpdated());
				request.header(HttpHeaders.IF_MODIFIED_SINCE, dateValue);
				logger.trace("Sending {} Header with value '{}'", HttpHeaders.IF_MODIFIED_SINCE, dateValue.toString());
			}
		}

		Response response = request.accept(Constants.CT_FHIR_JSON_NEW).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return referenceCleaner.cleanReferenceResourcesIfBundle(response.readEntity(resourceType));
		else if (oldValue != null && oldValue.hasMeta()
				&& (oldValue.getMeta().hasVersionId() || oldValue.getMeta().hasLastUpdated())
				&& Status.NOT_MODIFIED.getStatusCode() == response.getStatus())
			return oldValue;
		else
			throw handleError(response);
	}

	private String formatRfc7231(Date date)
	{
		if (date == null)
			return null;
		else
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(RFC_7231_FORMAT, Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			return dateFormat.format(date);
		}
	}

	@Override
	public <R extends Resource> boolean exists(Class<R> resourceType, String id)
	{
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(id, "id");

		Response response = getResource().path(resourceType.getAnnotation(ResourceDef.class).name()).path(id).request()
				.accept(Constants.CT_FHIR_JSON_NEW).head();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return true;
		else if (Status.NOT_FOUND.getStatusCode() == response.getStatus())
			return false;
		else
			throw handleError(response);
	}

	@Override
	public InputStream readBinary(String id, MediaType mediaType)
	{
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(mediaType, "mediaType");

		Response response = getResource().path("Binary").path(id).request().accept(mediaType).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return response.readEntity(InputStream.class);
		else
			throw handleError(response);
	}

	@Override
	public Resource read(String resourceTypeName, String id, String version)
	{
		Objects.requireNonNull(resourceTypeName, "resourceTypeName");
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(version, "version");
		if (!resourceTypeByNames.containsKey(resourceTypeName))
			throw new IllegalArgumentException("Resource of type " + resourceTypeName + " not supported");

		Response response = getResource().path(resourceTypeName).path(id).path("_history").path(version).request()
				.accept(Constants.CT_FHIR_JSON_NEW).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return referenceCleaner.cleanReferenceResourcesIfBundle(
					(Resource) response.readEntity(resourceTypeByNames.get(resourceTypeName)));
		else
			throw handleError(response);
	}

	@Override
	public <R extends Resource> R read(Class<R> resourceType, String id, String version)
	{
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(version, "version");

		Response response = getResource().path(resourceType.getAnnotation(ResourceDef.class).name()).path(id)
				.path("_history").path(version).request().accept(Constants.CT_FHIR_JSON_NEW).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return referenceCleaner.cleanReferenceResourcesIfBundle(response.readEntity(resourceType));
		else
			throw handleError(response);
	}

	@Override
	public <R extends Resource> boolean exists(Class<R> resourceType, String id, String version)
	{
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(version, "version");

		Response response = getResource().path(resourceType.getAnnotation(ResourceDef.class).name()).path(id)
				.path("_history").path(version).request().accept(Constants.CT_FHIR_JSON_NEW).head();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return true;
		else if (Status.NOT_FOUND.getStatusCode() == response.getStatus())
			return false;
		else
			throw handleError(response);
	}

	@Override
	public InputStream readBinary(String id, String version, MediaType mediaType)
	{
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(version, "version");
		Objects.requireNonNull(mediaType, "mediaType");

		Response response = getResource().path("Binary").path(id).path("_history").path(version).request()
				.accept(mediaType).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return response.readEntity(InputStream.class);
		else
			throw handleError(response);
	}

	@Override
	public boolean exists(IdType resourceTypeIdVersion)
	{
		Objects.requireNonNull(resourceTypeIdVersion, "resourceTypeIdVersion");
		Objects.requireNonNull(resourceTypeIdVersion.getResourceType(), "resourceTypeIdVersion.resourceType");
		Objects.requireNonNull(resourceTypeIdVersion.getIdPart(), "resourceTypeIdVersion.idPart");
		// version may be null

		WebTarget path = getResource().path(resourceTypeIdVersion.getResourceType())
				.path(resourceTypeIdVersion.getIdPart());

		if (resourceTypeIdVersion.hasVersionIdPart())
			path = path.path("_history").path(resourceTypeIdVersion.getVersionIdPart());

		Response response = path.request().accept(Constants.CT_FHIR_JSON_NEW).head();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return true;
		else if (Status.NOT_FOUND.getStatusCode() == response.getStatus())
			return false;
		else
			throw handleError(response);
	}

	@Override
	public Bundle search(Class<? extends Resource> resourceType, Map<String, List<String>> parameters)
	{
		Objects.requireNonNull(resourceType, "resourceType");

		WebTarget target = getResource().path(resourceType.getAnnotation(ResourceDef.class).name());
		if (parameters != null)
		{
			for (Entry<String, List<String>> entry : parameters.entrySet())
				target = target.queryParam(entry.getKey(), entry.getValue().toArray());
		}

		Response response = target.request().accept(Constants.CT_FHIR_JSON_NEW).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return referenceCleaner.cleanReferenceResourcesIfBundle(response.readEntity(Bundle.class));
		else
			throw handleError(response);
	}

	@Override
	public Bundle searchWithStrictHandling(Class<? extends Resource> resourceType, Map<String, List<String>> parameters)
	{
		Objects.requireNonNull(resourceType, "resourceType");

		WebTarget target = getResource().path(resourceType.getAnnotation(ResourceDef.class).name());
		if (parameters != null)
		{
			for (Entry<String, List<String>> entry : parameters.entrySet())
				target = target.queryParam(entry.getKey(), entry.getValue().toArray());
		}

		Response response = target.request().header(Constants.HEADER_PREFER, PreferHandlingType.STRICT.getHeaderValue())
				.accept(Constants.CT_FHIR_JSON_NEW).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return referenceCleaner.cleanReferenceResourcesIfBundle(response.readEntity(Bundle.class));
		else
			throw handleError(response);
	}

	@Override
	public CapabilityStatement getConformance()
	{
		Response response = getResource().path("metadata").request()
				.accept(Constants.CT_FHIR_JSON_NEW + "; fhirVersion=4.0").get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());

		if (Status.OK.getStatusCode() == response.getStatus())
			return response.readEntity(CapabilityStatement.class);
		else
			throw handleError(response);
	}

	@Override
	public StructureDefinition generateSnapshot(String url)
	{
		Objects.requireNonNull(url, "url");

		Parameters parameters = new Parameters();
		parameters.addParameter().setName("url").setValue(new UriType(url));

		Response response = getResource().path(StructureDefinition.class.getAnnotation(ResourceDef.class).name())
				.path("$snapshot").request().accept(Constants.CT_FHIR_JSON_NEW)
				.post(Entity.entity(parameters, Constants.CT_FHIR_JSON_NEW));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return response.readEntity(StructureDefinition.class);
		else
			throw handleError(response);
	}

	@Override
	public StructureDefinition generateSnapshot(StructureDefinition differential)
	{
		Objects.requireNonNull(differential, "differential");

		Parameters parameters = new Parameters();
		parameters.addParameter().setName("resource").setResource(differential);

		Response response = getResource().path(StructureDefinition.class.getAnnotation(ResourceDef.class).name())
				.path("$snapshot").request().accept(Constants.CT_FHIR_JSON_NEW)
				.post(Entity.entity(parameters, Constants.CT_FHIR_JSON_NEW));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return response.readEntity(StructureDefinition.class);
		else
			throw handleError(response);
	}

	@Override
	public BasicFhirWebserviceClient withRetry(int nTimes, long delayMillis)
	{
		if (nTimes < 0)
			throw new IllegalArgumentException("nTimes < 0");
		if (delayMillis < 0)
			throw new IllegalArgumentException("delayMillis < 0");

		return new BasicFhirWebserviceCientWithRetryImpl(this, nTimes, delayMillis);
	}

	@Override
	public BasicFhirWebserviceClient withRetryForever(long delayMillis)
	{
		if (delayMillis < 0)
			throw new IllegalArgumentException("delayMillis < 0");

		return new BasicFhirWebserviceCientWithRetryImpl(this, RETRY_FOREVER, delayMillis);
	}

	@Override
	public Bundle history(Class<? extends Resource> resourceType, String id, int page, int count)
	{
		WebTarget target = getResource();

		if (resourceType != null)
			target = target.path(resourceType.getAnnotation(ResourceDef.class).name());

		if (!StringUtils.isBlank(id))
			target = target.path(id);

		if (page != Integer.MIN_VALUE)
			target = target.queryParam("_page", page);

		if (count != Integer.MIN_VALUE)
			target = target.queryParam("_count", count);

		Response response = target.path("_history").request().accept(Constants.CT_FHIR_JSON_NEW).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			return response.readEntity(Bundle.class);
		else
			throw handleError(response);
	}
}
