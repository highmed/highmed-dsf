package org.highmed.fhir.adapter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.BaseResource;

import ca.uhn.fhir.context.FhirContext;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonFhirAdapter<T extends BaseResource> extends AbstractFhirAdapter<T>
		implements MessageBodyReader<T>, MessageBodyWriter<T>
{
	public static <T extends BaseResource> JsonFhirAdapter<T> create(FhirContext fhirContext, Class<T> resourceType)
	{
		return new JsonFhirAdapter<T>(fhirContext, resourceType);
	}

	private JsonFhirAdapter(FhirContext fhirContext, Class<T> resourceType)
	{
		super(resourceType, fhirContext.newJsonParser());
	}
}
