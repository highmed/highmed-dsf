package org.highmed.fhir.client;

import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.highmed.fhir.adapter.CapabilityStatementJsonFhirAdapter;
import org.highmed.fhir.adapter.CapabilityStatementXmlFhirAdapter;
import org.highmed.fhir.adapter.PatientJsonFhirAdapter;
import org.highmed.fhir.adapter.PatientXmlFhirAdapter;
import org.highmed.fhir.adapter.SubscriptionJsonFhirAdapter;
import org.highmed.fhir.adapter.SubscriptionXmlFhirAdapter;
import org.highmed.fhir.adapter.TaskJsonFhirAdapter;
import org.highmed.fhir.adapter.TaskXmlFhirAdapter;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.api.Constants;

public class FhirJerseyClient extends AbstractJerseyClient
{
	private static final Logger logger = LoggerFactory.getLogger(FhirJerseyClient.class);

	public FhirJerseyClient(String schemaHostPort, KeyStore trustStore, KeyStore keyStore, String keyStorePassword,
			String proxySchemeHostPort, String proxyUserName, String proxyPassword, int connectTimeout, int readTimeout,
			ObjectMapper objectMapper, FhirContext fhirContext)
	{
		super(schemaHostPort, trustStore, keyStore, keyStorePassword, proxySchemeHostPort, proxyUserName, proxyPassword,
				connectTimeout, readTimeout, objectMapper, components(fhirContext));
	}

	public static List<Object> components(FhirContext fhirContext)
	{
		return Arrays.asList(new PatientJsonFhirAdapter(fhirContext), new PatientXmlFhirAdapter(fhirContext),
				new TaskJsonFhirAdapter(fhirContext), new TaskXmlFhirAdapter(fhirContext),
				new SubscriptionJsonFhirAdapter(fhirContext), new SubscriptionXmlFhirAdapter(fhirContext),
				new CapabilityStatementJsonFhirAdapter(fhirContext),
				new CapabilityStatementXmlFhirAdapter(fhirContext));
	}

	public DomainResource create(DomainResource resource)
	{
		try (Response response = getResource().path(resource.getClass().getAnnotation(ResourceDef.class).name())
				.request().accept(Constants.CT_FHIR_JSON_NEW).post(Entity.entity(resource, Constants.CT_FHIR_JSON_NEW)))
		{
			logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
					response.getStatusInfo().getReasonPhrase());
			logger.debug("HTTP header Location: {}", response.getLocation());
			logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
			logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

			return response.readEntity(resource.getClass());
		}
	}

	public void getConformance()
	{
		try (Response response = getResource().path("metadata").request()
				.accept(Constants.CT_FHIR_JSON_NEW + "; fhirVersion=4.0").get())
		{
			logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
					response.getStatusInfo().getReasonPhrase());
		}
	}
}
