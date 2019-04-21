package org.highmed.fhir.help;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.adapter.AbstractFhirAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

public class ParameterConverter
{
	private static final Logger logger = LoggerFactory.getLogger(ParameterConverter.class);

	public static final String JSON_FORMAT = "json";
	public static final List<String> JSON_FORMATS = Arrays.asList(Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW,
			MediaType.APPLICATION_JSON);
	public static final String XML_FORMAT = "xml";
	public static final List<String> XML_FORMATS = Arrays.asList(Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW,
			MediaType.APPLICATION_XML, MediaType.TEXT_XML);

	private final ExceptionHandler exceptionHandler;

	public ParameterConverter(ExceptionHandler exceptionHandler)
	{
		this.exceptionHandler = exceptionHandler;
	}

	public UUID toUuid(String resourceTypeName, String id)
	{
		if (id == null)
			return null;

		// TODO control flow by exception
		try
		{
			return UUID.fromString(id);
		}
		catch (IllegalArgumentException e)
		{
			throw exceptionHandler.notFound(resourceTypeName, e);
		}
	}

	public MediaType getMediaType(UriInfo uri, HttpHeaders headers)
	{
		String format = uri.getQueryParameters().getFirst("_format");
		boolean pretty = "true".equals(uri.getQueryParameters().getFirst("_pretty"));
		String accept = headers.getHeaderString(HttpHeaders.ACCEPT);

		if (format == null || format.isBlank())
			return getMediaType(accept, pretty);
		else if (XML_FORMATS.contains(format) || JSON_FORMATS.contains(format))
			return getMediaType(format, pretty);
		else if (XML_FORMAT.equals(format))
			return mediaType("application", "fhir+xml", pretty);
		else if (JSON_FORMAT.equals(format))
			return mediaType("application", "fhir+json", pretty);
		else
			throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
	}

	private MediaType getMediaType(String mediaType, boolean pretty)
	{
		if (mediaType.contains(Constants.CT_FHIR_JSON_NEW))
			return mediaType("application", "fhir+json", pretty);
		else if (mediaType.contains(Constants.CT_FHIR_JSON))
			return mediaType("application", "json+fhir", pretty);
		else if (mediaType.contains(MediaType.APPLICATION_JSON))
			return mediaType("application", "json", pretty);
		else if (mediaType.contains(Constants.CT_FHIR_XML_NEW))
			return mediaType("application", "fhir+xml", pretty);
		else if (mediaType.contains(Constants.CT_FHIR_XML))
			return mediaType("application", "xml+fhir", pretty);
		else if (mediaType.contains(MediaType.APPLICATION_XML))
			return mediaType("application", "xml", pretty);
		else if (mediaType.contains(MediaType.TEXT_XML))
			return mediaType("text", "xml", pretty);
		else
		{
			logger.error("Media type '{}' not supported", mediaType);
			throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
		}
	}

	private MediaType mediaType(String type, String subtype, boolean pretty)
	{
		return new MediaType(type, subtype,
				!pretty ? null : Map.of(AbstractFhirAdapter.PRETTY, String.valueOf(pretty)));
	}

	public Integer getFirstInt(MultivaluedMap<String, String> queryParameters, String key)
	{
		String first = queryParameters.getFirst(key);
		try
		{
			return Integer.valueOf(first);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	/**
	 * replaces all occurrences of '+' with ' ' (a space) for all query parameter values
	 * 
	 * @param queryParameters
	 * @return
	 */
	public Map<String, List<String>> cleanQueryParameters(Map<String, List<String>> queryParameters)
	{
		Map<String, List<String>> cleaned = new HashMap<>((int) (queryParameters.size() / 0.75) + 1);
		for (Entry<String, List<String>> entry : queryParameters.entrySet())
			cleaned.put(entry.getKey(), cleanQueryParameterValues(entry.getValue()));
		return cleaned;
	}

	private List<String> cleanQueryParameterValues(List<String> queryParameterValues)
	{
		return queryParameterValues.stream().map(v -> v.replace('+', ' ')).collect(Collectors.toList());
	}
}
