package org.highmed.dsf.fhir.webservice.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Hex;
import org.highmed.dsf.fhir.webservice.base.AbstractBasicService;
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;

import ca.uhn.fhir.rest.api.Constants;

public class StaticResourcesServiceImpl extends AbstractBasicService implements StaticResourcesService
{
	private static final class CacheEntry
	{
		private final byte[] data;
		private final byte[] hash;
		private final String mimeType;

		CacheEntry(byte[] data, byte[] hash, String mimeType)
		{
			this.data = data;
			this.hash = hash;
			this.mimeType = mimeType;
		}

		byte[] getData()
		{
			return data;
		}

		EntityTag getTag()
		{
			return new EntityTag(Hex.encodeHexString(hash));
		}

		String getMimeType()
		{
			return mimeType;
		}
	}

	private static final class Cache
	{
		private final Map<String, SoftReference<CacheEntry>> entries = new HashMap<>();

		Optional<CacheEntry> get(String fileName)
		{
			SoftReference<CacheEntry> entry = entries.get(fileName);
			if (entry == null || entry.get() == null)
				return read(fileName);
			else
				return Optional.of(entry.get());
		}

		private Optional<CacheEntry> read(String fileName)
		{
			try (InputStream stream = StaticResourcesServiceImpl.class.getResourceAsStream("/static/" + fileName))
			{
				if (stream == null)
					return Optional.empty();
				else
				{
					byte[] data = stream.readAllBytes();
					byte[] hash = hash(data);
					String mimeType = mimeType(fileName);

					CacheEntry entry = new CacheEntry(data, hash, mimeType);
					entries.put(fileName, new SoftReference<>(entry));
					return Optional.of(entry);
				}
			}
			catch (IOException e)
			{
				throw new WebApplicationException(e);
			}
		}

		private byte[] hash(byte[] data)
		{
			try
			{
				MessageDigest digest = MessageDigest.getInstance("MD5");
				return digest.digest(data);
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		private String mimeType(String fileName)
		{
			String[] parts = fileName.split("\\.");
			return MIME_TYPE_BY_SUFFIX.get(parts[parts.length - 1]);
		}
	}

	private static final Map<String, String> MIME_TYPE_BY_SUFFIX = Map.of("css", "text/css", "js", "text/javascript",
			"html", "text/html", "pdf", "application/pdf", "png", "image/png", "svg", "image/svg+xml", "jpg",
			"image/jpeg");

	private final Cache cache = new Cache();

	@Override
	public Response getFile(String fileName, UriInfo uri, HttpHeaders headers)
	{
		if (fileName == null || fileName.isBlank())
			return Response.status(Status.NOT_FOUND).build();
		else if (!MIME_TYPE_BY_SUFFIX.keySet().stream().anyMatch(key -> fileName.endsWith(key)))
			return Response.status(Status.NOT_FOUND).build();
		else
		{
			Optional<CacheEntry> entry = cache.get(fileName);
			Optional<String> matchTag = Arrays.asList(Constants.HEADER_IF_NONE_MATCH, Constants.HEADER_IF_NONE_MATCH_LC)
					.stream().map(name -> headers.getHeaderString(name)).filter(h -> h != null).findFirst();

			return entry.map(toNotModifiedOrOkResponse(matchTag.orElse(""))).orElse(Response.status(Status.NOT_FOUND))
					.build();
		}
	}

	private Function<CacheEntry, ResponseBuilder> toNotModifiedOrOkResponse(String matchTag)
	{
		return entry ->
		{
			if (entry.getTag().getValue().equals(matchTag.replace("\"", "")))
				return Response.status(Status.NOT_MODIFIED);
			else
				return Response.ok(entry.getData(), MediaType.valueOf(entry.getMimeType())).tag(entry.getTag());
		};
	}
}
