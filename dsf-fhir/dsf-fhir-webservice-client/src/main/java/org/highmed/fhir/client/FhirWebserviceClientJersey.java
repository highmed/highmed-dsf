package org.highmed.fhir.client;

import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.highmed.dsf.fhir.adapter.AbstractFhirAdapter;
import org.highmed.dsf.fhir.adapter.BinaryJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.BinaryXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.BundleJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.BundleXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.CapabilityStatementJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.CapabilityStatementXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.CodeSystemJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.CodeSystemXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.EndpointJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.EndpointXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.GroupJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.GroupXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.HealthcareServiceJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.HealthcareServiceXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.LocationJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.LocationXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.NamingSystemJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.NamingSystemXmlFhirAdapter;
import org.highmed.dsf.fhir.adapter.OperationOutcomeJsonFhirAdapter;
import org.highmed.dsf.fhir.adapter.OperationOutcomeXmlFhirAdapter;
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
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
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
	private static final Logger logger = LoggerFactory.getLogger(FhirWebserviceClientJersey.class);

	public FhirWebserviceClientJersey(String baseUrl, KeyStore trustStore, KeyStore keyStore, String keyStorePassword,
			String proxySchemeHostPort, String proxyUserName, String proxyPassword, int connectTimeout, int readTimeout,
			ObjectMapper objectMapper, FhirContext fhirContext)
	{
		super(baseUrl, trustStore, keyStore, keyStorePassword, proxySchemeHostPort, proxyUserName, proxyPassword,
				connectTimeout, readTimeout, objectMapper, components(fhirContext));
	}

	public static List<AbstractFhirAdapter<?>> components(FhirContext fhirContext)
	{
		return Arrays.asList(new BinaryJsonFhirAdapter(fhirContext), new BinaryXmlFhirAdapter(fhirContext),
				new BundleJsonFhirAdapter(fhirContext), new BundleXmlFhirAdapter(fhirContext),
				new CapabilityStatementJsonFhirAdapter(fhirContext), new CapabilityStatementXmlFhirAdapter(fhirContext),
				new CodeSystemJsonFhirAdapter(fhirContext), new CodeSystemXmlFhirAdapter(fhirContext),
				new GroupJsonFhirAdapter(fhirContext), new GroupXmlFhirAdapter(fhirContext),
				new EndpointJsonFhirAdapter(fhirContext), new EndpointXmlFhirAdapter(fhirContext),
				new HealthcareServiceJsonFhirAdapter(fhirContext), new HealthcareServiceXmlFhirAdapter(fhirContext),
				new LocationJsonFhirAdapter(fhirContext), new LocationXmlFhirAdapter(fhirContext),
				new NamingSystemJsonFhirAdapter(fhirContext), new NamingSystemXmlFhirAdapter(fhirContext),
				new OperationOutcomeJsonFhirAdapter(fhirContext), new OperationOutcomeXmlFhirAdapter(fhirContext),
				new OrganizationJsonFhirAdapter(fhirContext), new OrganizationXmlFhirAdapter(fhirContext),
				new ParametersJsonFhirAdapter(fhirContext), new ParametersXmlFhirAdapter(fhirContext),
				new PatientJsonFhirAdapter(fhirContext), new PatientXmlFhirAdapter(fhirContext),
				new PractitionerJsonFhirAdapter(fhirContext), new PractitionerXmlFhirAdapter(fhirContext),
				new PractitionerRoleJsonFhirAdapter(fhirContext), new PractitionerRoleXmlFhirAdapter(fhirContext),
				new ProvenanceJsonFhirAdapter(fhirContext), new ProvenanceXmlFhirAdapter(fhirContext),
				new ResearchStudyJsonFhirAdapter(fhirContext), new ResearchStudyXmlFhirAdapter(fhirContext),
				new StructureDefinitionJsonFhirAdapter(fhirContext), new StructureDefinitionXmlFhirAdapter(fhirContext),
				new SubscriptionJsonFhirAdapter(fhirContext), new SubscriptionXmlFhirAdapter(fhirContext),
				new TaskJsonFhirAdapter(fhirContext), new TaskXmlFhirAdapter(fhirContext),
				new ValueSetJsonFhirAdapter(fhirContext), new ValueSetXmlFhirAdapter(fhirContext));
	}

	@Override
	public <R extends Resource> R create(R resource)
	{
		Objects.requireNonNull(resource, "resource");

		Response response = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name()).request()
				.accept(Constants.CT_FHIR_JSON_NEW).post(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header Location: {}", response.getLocation());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Status.CREATED.getStatusCode() == response.getStatus())
		{
			@SuppressWarnings("unchecked")
			R read = (R) response.readEntity(resource.getClass());
			return read;
		}
		else
			throw new WebApplicationException(response);
	}

	@Override
	public <R extends Resource> R createConditionaly(R resource, String ifNoneExistCriteria)
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(ifNoneExistCriteria, "ifNoneExistCriteria");

		Response response = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name()).request()
				.header(Constants.HEADER_IF_NONE_EXIST, ifNoneExistCriteria).accept(Constants.CT_FHIR_JSON_NEW)
				.post(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header Location: {}", response.getLocation());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Status.CREATED.getStatusCode() == response.getStatus())
		{
			@SuppressWarnings("unchecked")
			R read = (R) response.readEntity(resource.getClass());
			return read;
		}
		else
			throw new WebApplicationException(response);
	}

	@Override
	public <R extends Resource> R update(R resource)
	{
		Objects.requireNonNull(resource, "resource");

		Builder builder = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name())
				.path(resource.getIdElement().getIdPart()).request().accept(Constants.CT_FHIR_JSON_NEW);

		if (resource.getMeta().hasVersionId())
			builder.header(Constants.HEADER_IF_MATCH, new EntityTag(resource.getMeta().getVersionId(), true));

		Response response = builder.put(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Status.OK.getStatusCode() == response.getStatus())
		{
			@SuppressWarnings("unchecked")
			R read = (R) response.readEntity(resource.getClass());
			return read;
		}
		else
			throw new WebApplicationException(response);
	}

	@Override
	public <R extends Resource> R updateConditionaly(R resource, Map<String, List<String>> criteria)
	{
		Objects.requireNonNull(resource, "resource");
		Objects.requireNonNull(criteria, "criteria");
		if (criteria.isEmpty())
			throw new IllegalArgumentException("criteria map empty");

		WebTarget target = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name());

		for (Entry<String, List<String>> entry : criteria.entrySet())
			target = target.queryParam(entry.getKey(), entry.getValue().toArray());

		Builder builder = target.request().accept(Constants.CT_FHIR_JSON_NEW);

		if (resource.getMeta().hasVersionId())
			builder.header(Constants.HEADER_IF_MATCH, new EntityTag(resource.getMeta().getVersionId(), true));

		Response response = builder.put(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Status.CREATED.getStatusCode() == response.getStatus() || Status.OK.getStatusCode() == response.getStatus())
		{
			@SuppressWarnings("unchecked")
			R read = (R) response.readEntity(resource.getClass());
			return read;
		}
		else
			throw new WebApplicationException(response);
	}

	@Override
	public void delete(Class<? extends Resource> resourceClass, String id)
	{
		Objects.requireNonNull(resourceClass, "resourceClass");
		Objects.requireNonNull(id, "id");

		Response response = getResource().path(resourceClass.getAnnotation(ResourceDef.class).name()).path(id).request()
				.accept(Constants.CT_FHIR_JSON_NEW).delete();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Status.OK.getStatusCode() != response.getStatus() && Status.NO_CONTENT.getStatusCode() != response
				.getStatus())
			throw new WebApplicationException(response);
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

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Status.OK.getStatusCode() != response.getStatus() && Status.NO_CONTENT.getStatusCode() != response
				.getStatus())
			throw new WebApplicationException(response);
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
			throw new WebApplicationException(response);
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
			throw new WebApplicationException(response);
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
			throw new WebApplicationException(response);
	}

	@Override
	public <R extends Resource> R read(Class<R> resourceType, String id)
	{
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(id, "id");

		Response response = getResource().path(resourceType.getAnnotation(ResourceDef.class).name()).path(id).request()
				.accept(Constants.CT_FHIR_JSON_NEW).get();

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		if (Status.OK.getStatusCode() == response.getStatus())
			// TODO remove workaround if HAPI bug fixed
			return fixBundle(resourceType, response.readEntity(resourceType));
		else
			throw new WebApplicationException(response);
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
			throw new WebApplicationException(response);
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
			throw new WebApplicationException(response);
	}

	// FIXME bug in HAPI framework
	// TODO workaround using ReferenceExtractorImpl to remove all reference->resources
	private <R extends Resource> R fixBundle(Class<R> resourceType, R readEntity)
	{
		if (Bundle.class.equals(resourceType))
		{
			Bundle b = (Bundle) readEntity;
			b.getEntry().stream().filter(e -> e.hasResource() && e.getResource() instanceof Organization)
					.map(e -> (Organization) e.getResource()).forEach(this::fixOrganization);
			b.getEntry().stream().filter(e -> e.hasResource() && e.getResource() instanceof Endpoint)
					.map(e -> (Endpoint) e.getResource()).forEach(this::fixEndpoint);
		}

		return readEntity;
	}

	private void fixOrganization(Organization organization)
	{
		organization.getEndpoint().forEach(ref -> ref.setResource(null));
	}

	private void fixEndpoint(Endpoint endpoint)
	{
		endpoint.getManagingOrganization().setResource(null);
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
			return fixBundle(resourceType, response.readEntity(resourceType));
		else
			throw new WebApplicationException(response);
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
			throw new WebApplicationException(response);
	}

	@Override
	public <R extends Resource> Bundle search(Class<R> resourceType, Map<String, List<String>> parameters)
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
			return response.readEntity(Bundle.class);
		else
			throw new WebApplicationException(response);
	}

	// private <R extends DomainResource> List<R> bundleToList(Class<R> resourceType, Bundle bundle)
	// {
	// return bundle.getEntry().stream().filter(c -> resourceType.isInstance(c.getResource()))
	// .map(c -> resourceType.cast(c.getResource())).collect(Collectors.toList());
	// }

	@Override
	public Bundle postBundle(Bundle bundle)
	{
		Objects.requireNonNull(bundle, "bundle");

		Response response = getResource().request().accept(Constants.CT_FHIR_JSON_NEW)
				.post(Entity.entity(bundle, Constants.CT_FHIR_JSON_NEW));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header Location: {}", response.getLocation());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Status.OK.getStatusCode() == response.getStatus())
			return response.readEntity(Bundle.class);
		else
			throw new WebApplicationException(response);
	}
}
