package org.highmed.dsf.fhir.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.hl7.fhir.r4.model.BaseResource;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.parser.IParser;

public abstract class AbstractFhirAdapter<T extends BaseResource> implements MessageBodyReader<T>, MessageBodyWriter<T>
{
	public static final String PRETTY = "pretty";

	private final Class<T> resourceType;
	private final Supplier<IParser> parser;

	protected AbstractFhirAdapter(Class<T> resourceType, Supplier<IParser> parser)
	{
		this.resourceType = resourceType;
		this.parser = parser;
	}

	public final Class<? extends BaseResource> getResourceType()
	{
		return resourceType;
	}

	public final String getResourceTypeName()
	{
		return getResourceType().getAnnotation(ResourceDef.class).name();
	}

	private IParser getParser(MediaType mediaType)
	{
		/* Parsers are not guaranteed to be thread safe */
		IParser p = parser.get();
		p.setStripVersionsFromReferences(false);
		p.setOverrideResourceIdWithBundleEntryFullUrl(false);

		if (mediaType != null && "true".equals(mediaType.getParameters().getOrDefault(PRETTY, "false")))
			p.setPrettyPrint(true);

		return p;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return resourceType.equals(type);
	}

	@Override
	public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException
	{
		getParser(mediaType).encodeResourceToWriter(t, new OutputStreamWriter(entityStream));
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return resourceType.equals(type);
	}

	@Override
	public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException
	{
		return fixResource(getParser(null).parseResource(type, new InputStreamReader(entityStream)));
	}

	protected T fixResource(T resource)
	{
		return resource;
	}
}
