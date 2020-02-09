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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Hex;
import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;

import ca.uhn.fhir.rest.api.Constants;

public class StaticResourcesServiceImpl implements StaticResourcesService
{
	private static final class CacheEntry
	{
		private final byte[] data;
		private final byte[] hash;

		CacheEntry(byte[] data, byte[] hash)
		{
			this.data = data;
			this.hash = hash;
		}

		byte[] getData()
		{
			return data;
		}

		EntityTag getTag()
		{
			return new EntityTag(Hex.encodeHexString(hash));
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
			InputStream stream = StaticResourcesServiceImpl.class.getResourceAsStream("/static/" + fileName);
			if (stream == null)
				return Optional.empty();
			else
			{
				try
				{
					byte[] data = stream.readAllBytes();
					byte[] hash = hash(data);

					CacheEntry entry = new CacheEntry(data, hash);
					entries.put(fileName, new SoftReference<>(entry));
					return Optional.of(entry);
				}
				catch (IOException e)
				{
					throw new WebApplicationException(e);
				}
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
	}

	private static final Map<String, String> MIME_TYPE_BY_SUFFIX = Map.of("css", "text/css", "js", "text/javascript",
			"html", "text/html", "pdf", "application/pdf", "png", "image/png", "svg", "image/svg+xml", "jpg",
			"image/jpeg");

	private final Cache cache = new Cache();

	private final String path;

	public StaticResourcesServiceImpl(String path)
	{
		this.path = path;
	}

	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public void setUserProvider(UserProvider provider)
	{
	}

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

			return entry.map(e ->
			{
				if (e.getTag().getValue().equals(matchTag.orElse("").replace("\"", "")))
					return Response.status(Status.NOT_MODIFIED);
				else
				{
					String[] parts = fileName.split("\\.");
					return Response.ok(e.getData(), MediaType.valueOf(MIME_TYPE_BY_SUFFIX.get(parts[parts.length - 1])))
							.tag(e.getTag());
				}
			}).orElse(Response.status(Status.NOT_FOUND)).build();
		}
	}
}
