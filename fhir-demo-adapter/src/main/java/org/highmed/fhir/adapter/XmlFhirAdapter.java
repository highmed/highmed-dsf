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
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class XmlFhirAdapter<T extends BaseResource> extends AbstractFhirAdapter<T>
		implements MessageBodyReader<T>, MessageBodyWriter<T>
{
	public static <T extends BaseResource> XmlFhirAdapter<T> create(FhirContext fhirContext, Class<T> resourceType)
	{
		return new XmlFhirAdapter<T>(fhirContext, resourceType);
	}

	public XmlFhirAdapter(FhirContext fhirContext, Class<T> resourceType)
	{
		super(resourceType, fhirContext.newXmlParser());
	}
}
