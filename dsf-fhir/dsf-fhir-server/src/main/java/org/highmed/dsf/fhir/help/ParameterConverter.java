package org.highmed.dsf.fhir.help;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.adapter.AbstractFhirAdapter;
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

	/**
	 * @param resourceTypeName
	 *            not <code>null</code>, will be part of the {@link WebApplicationException} if the given id can't be
	 *            parsed (aka is not a {@link UUID})
	 * @param id
	 * @return <code>null</code> if the given id is <code>null</code>
	 */
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

	public Integer getFirstInt(Map<String, List<String>> queryParameters, String key)
	{
		List<String> listForKey = queryParameters.getOrDefault(key, Collections.emptyList());
		if (listForKey.isEmpty())
			return null;
		else
		{
			// TODO control flow by exception
			try
			{
				return Integer.valueOf(listForKey.get(0));
			}
			catch (NumberFormatException e)
			{
				return null;
			}
		}
	}

	/**
	 * URL-decodes all query-parameter values
	 * 
	 * @param queryParameters
	 * @return
	 */
	public Map<String, List<String>> urlDecodeQueryParameters(Map<String, List<String>> queryParameters)
	{
		Map<String, List<String>> cleaned = new LinkedHashMap<>((int) (queryParameters.size() / 0.75) + 1);
		for (Entry<String, List<String>> entry : queryParameters.entrySet())
			cleaned.put(entry.getKey(), urlDecodeQueryParameter(entry.getValue()));
		return cleaned;
	}

	private List<String> urlDecodeQueryParameter(List<String> queryParameterValues)
	{
		return queryParameterValues.stream().map(v -> URLDecoder.decode(v, StandardCharsets.UTF_8))
				.collect(Collectors.toList());
	}

	/**
	 * @param eTagValue
	 *            ETag string value
	 * @return {@link Optional} of {@link EntityTag} for the given value or {@link Optional#empty()} if the given value
	 *         could not be parsed or was null/blank
	 */
	public Optional<EntityTag> toEntityTag(String eTagValue)
	{
		if (eTagValue == null || eTagValue.isBlank())
			return Optional.empty();

		try
		{
			EntityTag eTag = EntityTag.valueOf(eTagValue);
			if (eTag.isWeak())
				return Optional.of(eTag);
			else
			{
				logger.warn("{} not a weak ETag", eTag.getValue());
				return Optional.empty();
			}
		}
		catch (IllegalArgumentException e)
		{
			logger.warn("Unable to parse ETag value", e);
			return Optional.empty();
		}
	}

	/**
	 * @param tag
	 * @return {@link Optional} long version for the given tag or {@link Optional#empty()} if the given tags value could
	 *         not be parsed as long or was null/blank
	 */
	public Optional<Long> toVersion(EntityTag tag)
	{
		if (tag == null || tag.getValue() == null || tag.getValue().isBlank())
			return Optional.empty();

		return toVersion(tag.getValue());
	}

	/**
	 * @param version
	 * @return {@link Optional} long version for the given {@link String} value or {@link Optional#empty()} if the given
	 *         {@link String} value could not be parsed as long or was null/blank
	 */
	public Optional<Long> toVersion(String version)
	{
		if (version == null || version.isBlank())
			return Optional.empty();

		try
		{
			return Optional.of(Long.parseLong(version));
		}
		catch (NumberFormatException e)
		{
			logger.warn("Version not a Long value", e);
			return Optional.empty();
		}
	}
}
