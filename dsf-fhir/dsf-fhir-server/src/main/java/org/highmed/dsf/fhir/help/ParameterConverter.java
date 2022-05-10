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
import org.highmed.dsf.fhir.prefer.PreferHandlingType;
import org.highmed.dsf.fhir.prefer.PreferReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

public class ParameterConverter
{
	private static final Logger logger = LoggerFactory.getLogger(ParameterConverter.class);

	public static final String HTML_FORMAT = "html";
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
	 *            may be <code>null</code>
	 * @return <code>null</code> if the given id is <code>null</code>
	 */
	public UUID toUuid(String resourceTypeName, String id)
	{
		if (id == null)
			return null;

		return toUuid(id).orElseThrow(() -> exceptionHandler.notFound(resourceTypeName));
	}

	/**
	 * @param id
	 *            may be <code>null</code>
	 * @return {@link Optional#empty()} if the given id is <code>null</code> or is not a {@link UUID}
	 */
	public Optional<UUID> toUuid(String id)
	{
		if (id == null)
			return Optional.empty();

		// TODO control flow by exception
		try
		{
			return Optional.of(UUID.fromString(id));
		}
		catch (IllegalArgumentException e)
		{
			return Optional.empty();
		}
	}

	public MediaType getMediaTypeThrowIfNotSupported(UriInfo uri, HttpHeaders headers) throws WebApplicationException
	{
		return getMediaTypeIfSupported(uri, headers).orElseThrow(() ->
		{
			logger.warn("Media type not supported");
			return new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
		});
	}

	public Optional<MediaType> getMediaTypeIfSupported(UriInfo uri, HttpHeaders headers)
	{
		String format = uri.getQueryParameters().getFirst("_format");
		boolean pretty = "true".equals(uri.getQueryParameters().getFirst("_pretty"));
		String accept = headers.getHeaderString(HttpHeaders.ACCEPT);

		if (format == null || format.isBlank())
			return getMediaType(accept, pretty);

		else if (XML_FORMATS.contains(format) || JSON_FORMATS.contains(format) || MediaType.TEXT_HTML.equals(format))
			return getMediaType(format, pretty);
		else if (XML_FORMAT.equals(format))
			return Optional.of(mediaType("application", "fhir+xml", pretty));
		else if (JSON_FORMAT.equals(format))
			return Optional.of(mediaType("application", "fhir+json", pretty));
		else if (HTML_FORMAT.equals(format))
			return Optional.of(mediaType("text", "html", pretty));
		else
			return Optional.empty();
	}

	private Optional<MediaType> getMediaType(String mediaType, boolean pretty)
	{
		if (mediaType == null || mediaType.isBlank())
			mediaType = MediaType.WILDCARD;

		if (mediaType.contains(MediaType.TEXT_HTML))
			return Optional.of(mediaType("text", "html", pretty));
		else if (mediaType.contains(Constants.CT_FHIR_JSON_NEW))
			return Optional.of(mediaType("application", "fhir+json", pretty));
		else if (mediaType.contains(Constants.CT_FHIR_JSON))
			return Optional.of(mediaType("application", "json+fhir", pretty));
		else if (mediaType.contains(MediaType.APPLICATION_JSON))
			return Optional.of(mediaType("application", "json", pretty));
		else if (mediaType.contains(Constants.CT_FHIR_XML_NEW))
			return Optional.of(mediaType("application", "fhir+xml", pretty));
		else if (mediaType.contains(Constants.CT_FHIR_XML))
			return Optional.of(mediaType("application", "xml+fhir", pretty));
		else if (mediaType.contains(MediaType.APPLICATION_XML))
			return Optional.of(mediaType("application", "xml", pretty));
		else if (mediaType.contains(MediaType.TEXT_XML))
			return Optional.of(mediaType("text", "xml", pretty));
		else if (mediaType.contains(MediaType.WILDCARD))
			return Optional.of(mediaType("application", "fhir+xml", pretty));
		else
			return Optional.empty();
	}

	private MediaType mediaType(String type, String subtype, boolean pretty)
	{
		return new MediaType(type, subtype,
				!pretty ? null : Map.of(AbstractFhirAdapter.PRETTY, String.valueOf(pretty)));
	}

	public PreferReturnType getPreferReturn(HttpHeaders headers)
	{
		List<String> preferHeaders = headers.getRequestHeader(Constants.HEADER_PREFER);

		if (preferHeaders == null)
			return PreferReturnType.REPRESENTATION;
		else
			return preferHeaders.stream().map(PreferReturnType::fromString).findFirst()
					.orElse(PreferReturnType.REPRESENTATION);
	}

	public PreferHandlingType getPreferHandling(HttpHeaders headers)
	{
		List<String> preferHeaders = headers.getRequestHeader(Constants.HEADER_PREFER);

		if (preferHeaders == null)
			return PreferHandlingType.LENIENT;
		else
			return preferHeaders.stream().map(PreferHandlingType::fromString).findFirst()
					.orElse(PreferHandlingType.LENIENT);
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
	 *            not <code>null</code>
	 * @return {@link Map} containing the supplied query-parameters in URL-decoded form
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
	 *            may be <code>null</code>
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
	 *            may be <code>null</code>
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
