package org.highmed.fhir.help;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import ca.uhn.fhir.rest.api.Constants;

public class ParameterConverter
{
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

	public String toSpecialMimeType(String format)
	{
		if (format == null || format.isBlank())
			return null;
		if (XML_FORMATS.contains(format) || JSON_FORMATS.contains(format))
			return format;
		else if (XML_FORMAT.equals(format))
			return Constants.CT_FHIR_XML_NEW;
		else if (JSON_FORMAT.equals(format))
			return Constants.CT_FHIR_JSON_NEW;
		else
			throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
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
}
