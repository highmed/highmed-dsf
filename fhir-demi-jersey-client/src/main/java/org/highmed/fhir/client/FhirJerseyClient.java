package org.highmed.fhir.client;

import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.highmed.fhir.adapter.PatientJsonAdapter;
import org.highmed.fhir.adapter.PatientXmlAdapter;
import org.hl7.fhir.r4.model.BaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;

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
		return Arrays.asList(new PatientJsonAdapter(fhirContext), new PatientXmlAdapter(fhirContext));
	}

	public URI create(BaseResource resource)
	{
		try (Response response = getResource().path(resource.getClass().getSimpleName()).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(resource, MediaType.APPLICATION_JSON_TYPE)))
		{
			logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
					response.getStatusInfo().getReasonPhrase());

			return null;
		}
	}
}
