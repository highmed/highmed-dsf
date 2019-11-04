package org.highmed.openehr.client;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.highmed.openehr.model.structure.Request;
import org.highmed.openehr.model.structure.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenehrWebserviceClientJersey extends AbstractJerseyClient implements OpenehrWebserviceClient
{
	private static final Logger logger = LoggerFactory.getLogger(OpenehrWebserviceClientJersey.class);

	public static final String OPENEHR_QUERY_PATH = "query/aql";

	public OpenehrWebserviceClientJersey(String baseUrl, String basicAuthUsername, String basicAuthPassword,
			int connectTimeout, int readTimeout, ObjectMapper objectMapper)
	{
		super(baseUrl, basicAuthUsername, basicAuthPassword, connectTimeout, readTimeout, objectMapper);
	}

	@Override
	public ResultSet query(String query, MultivaluedMap<String, Object> headers)
	{
		Response response = getResource().path(OPENEHR_QUERY_PATH).request().headers(headers)
				.header("Accept", MediaType.APPLICATION_JSON).header("Content-Type", MediaType.APPLICATION_JSON)
				.post(Entity.entity(new Request(query, null, null, null), MediaType.APPLICATION_JSON));

		logger.debug("HTTP {}: {}", response.getStatusInfo().getStatusCode(),
				response.getStatusInfo().getReasonPhrase());
		logger.debug("HTTP header Location: {}", response.getLocation());
		logger.debug("HTTP header ETag: {}", response.getHeaderString(HttpHeaders.ETAG));
		logger.debug("HTTP header Last-Modified: {}", response.getHeaderString(HttpHeaders.LAST_MODIFIED));

		if (Response.Status.OK.getStatusCode() == response.getStatus())
		{
			return response.readEntity(ResultSet.class);
		}
		else
			throw new WebApplicationException(response);
	}
}
