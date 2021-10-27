package org.highmed.openehr.client.impl;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.model.structure.Request;
import org.highmed.openehr.model.structure.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenEhrClientJersey extends AbstractJerseyClient implements OpenEhrClient
{
	private static final Logger logger = LoggerFactory.getLogger(OpenEhrClientJersey.class);

	public static final String OPENEHR_QUERY_PATH = "query/aql";

	private final ObjectMapper objectMapper;

	public OpenEhrClientJersey(String baseUrl, String basicAuthUsername, String basicAuthPassword,
			String trustCertificatesFile, int connectTimeout, int readTimeout, ObjectMapper objectMapper)
			throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
	{
		super(baseUrl, basicAuthUsername, basicAuthPassword, trustCertificatesFile, connectTimeout, readTimeout,
				objectMapper);

		this.objectMapper = objectMapper;
	}

	@Override
	public ResultSet query(String query, MultivaluedMap<String, Object> headers)
	{
		logger.debug("Sending query: {}", query);

		Response response = getResource().path(OPENEHR_QUERY_PATH).request().headers(headers)
				.header("Accept", MediaType.APPLICATION_JSON).header("Content-Type", MediaType.APPLICATION_JSON)
				.post(Entity.entity(new Request(query, null, null, null, null), MediaType.APPLICATION_JSON));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header Location: {}", response.getLocation());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Response.Status.OK.getStatusCode() == response.getStatus())
		{
			ResultSet resultSet = response.readEntity(ResultSet.class);

			logResponse(resultSet);
			return resultSet;
		}
		else
		{
			response.close();

			logger.warn("Error while executing query, HTTP {}: {}", response.getStatusInfo().getStatusCode(),
					response.getStatusInfo().getReasonPhrase());
			throw new WebApplicationException("Error while executing query, HTTP "
					+ response.getStatusInfo().getStatusCode() + ": " + response.getStatusInfo().getReasonPhrase());
		}
	}

	private void logResponse(ResultSet resultSet)
	{
		if (logger.isDebugEnabled())
		{
			try
			{
				logger.debug("Received ResultSet: {}", objectMapper.writeValueAsString(resultSet));
			}
			catch (JsonProcessingException exception)
			{
				logger.warn("Could not parse received ResultSet, reason: {}", exception.getMessage());
				throw new RuntimeException(exception);
			}
		}
	}
}
