package org.highmed.dsf.fhir.webservice.jaxrs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.webservice.specification.BinaryService;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.Constants;

@Path(BinaryServiceJaxrs.PATH)
public class BinaryServiceJaxrs extends AbstractResourceServiceJaxrs<Binary, BinaryService> implements BinaryService
{
	public static final String PATH = "Binary";

	private static final Logger logger = LoggerFactory.getLogger(BinaryServiceJaxrs.class);

	private final String[] FHIR_MEDIA_TYPES = { Constants.CT_FHIR_XML_NEW, Constants.CT_FHIR_JSON_NEW,
			Constants.CT_FHIR_XML, Constants.CT_FHIR_JSON };
	private final ParameterConverter parameterConverter;

	public BinaryServiceJaxrs(BinaryService delegate, ParameterConverter parameterConverter)
	{
		super(delegate);

		this.parameterConverter = parameterConverter;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(parameterConverter, "parameterConverter");
	}

	@POST
	@Consumes
	@Produces({ MediaType.TEXT_HTML, Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON,
			Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	@Override
	public Response create(InputStream in, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		logger.trace("POST {}", uri.getRequestUri().toString());

		try (in)
		{
			String securityContext = getSecurityContext(headers);
			String contentType = getContentType(headers);
			byte[] content = in.readAllBytes();

			Binary resource = createBinary(contentType, content, securityContext);
			return delegate.create(resource, uri, headers);
		}
		catch (IOException e)
		{
			throw new WebApplicationException(e);
		}
	}

	private Binary createBinary(String contentType, byte[] content, String securityContextReference)
	{
		Binary resource = new Binary();
		resource.setContentType(contentType);
		resource.setContent(content);
		resource.setSecurityContext(new Reference(securityContextReference));
		return resource;
	}

	private String getSecurityContext(HttpHeaders headers)
	{
		List<String> securityContexts = headers.getRequestHeader(Constants.HEADER_X_SECURITY_CONTEXT);
		if (securityContexts.size() == 1)
			return securityContexts.get(0);
		else if (securityContexts.size() > 1)
			throw new WebApplicationException(Status.BAD_REQUEST);

		securityContexts = headers.getRequestHeader(Constants.HEADER_X_SECURITY_CONTEXT.toLowerCase());
		if (securityContexts.size() == 1)
			return securityContexts.get(0);
		else if (securityContexts.size() > 1)
			throw new WebApplicationException(Status.BAD_REQUEST);

		throw new WebApplicationException(Status.BAD_REQUEST);
	}

	private String getContentType(HttpHeaders headers)
	{
		List<String> contentType = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE);
		if (contentType.size() == 1)
			return contentType.get(0);
		else if (contentType.size() > 1)
			throw new WebApplicationException(Status.BAD_REQUEST);

		contentType = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE.toLowerCase());
		if (contentType.size() == 1)
			return contentType.get(0);
		else if (contentType.size() > 1)
			throw new WebApplicationException(Status.BAD_REQUEST);

		throw new WebApplicationException(Status.BAD_REQUEST);
	}

	private String getAccept(HttpHeaders headers)
	{
		List<String> accept = headers.getRequestHeader(HttpHeaders.ACCEPT);
		if (accept.size() == 1)
		{
			String accept0 = accept.get(0);
			if (accept0 != null && !accept0.isBlank())
				return accept0;
		}

		throw new WebApplicationException(Status.BAD_REQUEST);
	}

	@GET
	@Path("/{id}")
	@Produces
	@Override
	public Response read(@PathParam("id") String id, @Context UriInfo uri, @Context HttpHeaders headers)
	{
		Response read = super.read(id, uri, headers);

		if (read.getEntity() instanceof Binary && !isFhirRequest(uri, headers))
		{
			Binary binary = (Binary) read.getEntity();
			if (mediaTypeMatches(headers, binary))
				return toStream(binary);
			else
				return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		else
			return read;
	}

	private boolean mediaTypeMatches(HttpHeaders headers, Binary binary)
	{
		String accept = getAccept(headers);
		MediaType acceptMediaType = MediaType.valueOf(accept);
		MediaType binaryMediaType = MediaType.valueOf(binary.getContentType());
		boolean compatible = acceptMediaType.isCompatible(binaryMediaType);
		return compatible;
	}

	private Response toStream(Binary binary)
	{
		String contentType = binary.getContentType();
		byte[] content = binary.getContent();

		ResponseBuilder b = Response.status(Status.OK).entity(new ByteArrayInputStream(content));
		b = b.type(contentType);

		if (binary.getMeta() != null && binary.getMeta().getLastUpdated() != null
				&& binary.getMeta().getVersionId() != null)
		{
			b = b.lastModified(binary.getMeta().getLastUpdated());
			b = b.tag(new EntityTag(binary.getMeta().getVersionId(), true));
		}

		if (binary.hasSecurityContext() && binary.getSecurityContext().hasReference())
		{
			// Not setting header for logical references
			b.header(Constants.HEADER_X_SECURITY_CONTEXT, binary.getSecurityContext().getReference());
		}

		return b.build();
	}

	@GET
	@Path("/{id}/_history/{version}")
	@Produces
	@Override
	public Response vread(@PathParam("id") String id, @PathParam("version") long version, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		Response read = super.vread(id, version, uri, headers);

		if (read.getEntity() instanceof Binary && !isFhirRequest(uri, headers))
		{
			Binary binary = (Binary) read.getEntity();
			if (mediaTypeMatches(headers, binary))
				return toStream(binary);
			else
				return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		else
			return read;
	}

	private boolean isFhirRequest(UriInfo uri, HttpHeaders headers)
	{
		String accept = headers.getHeaderString(HttpHeaders.ACCEPT);
		return Arrays.stream(FHIR_MEDIA_TYPES).anyMatch(f -> f.equals(accept)) || parameterConverter
				.getMediaTypeIfSupported(uri, headers).map(m -> !m.equals(MediaType.valueOf(accept))).orElse(false);
	}

	@PUT
	@Path("/{id}")
	@Consumes
	@Produces({ MediaType.TEXT_HTML, Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON,
			Constants.CT_FHIR_XML, Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	@Override
	public Response update(@PathParam("id") String id, InputStream in, @Context UriInfo uri,
			@Context HttpHeaders headers)
	{
		logger.trace("PUT {}", uri.getRequestUri().toString());

		try (in)
		{
			String securityContext = getSecurityContext(headers);
			String contentType = getContentType(headers);
			byte[] content = in.readAllBytes();

			Binary resource = createBinary(contentType, content, securityContext);
			return delegate.update(id, resource, uri, headers);
		}
		catch (IOException e)
		{
			throw new WebApplicationException(e);
		}
	}
}
