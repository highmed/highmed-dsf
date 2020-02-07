package org.highmed.dsf.fhir.webservice.impl;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.authentication.UserProvider;
import org.highmed.dsf.fhir.webservice.specification.StaticResourcesService;

public class StaticResourcesServiceImpl implements StaticResourcesService
{
	private static final Map<String, String> MIME_TYPE_BY_SUFFIX = Map.of("css", "text/css", "js", "text/javascript",
			"html", "text/html", "pdf", "application/pdf", "png", "image/png", "svg", "image/svg+xml", "jpg",
			"image/jpeg");

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
			InputStream stream = StaticResourcesServiceImpl.class.getResourceAsStream("/static/" + fileName);
			if (stream == null)
				return Response.status(Status.NOT_FOUND).build();
			else
			{
				String[] parts = fileName.split("\\.");

				return Response.ok(stream, MediaType.valueOf(MIME_TYPE_BY_SUFFIX.get(parts[parts.length - 1]))).build();
			}
		}
	}
}
