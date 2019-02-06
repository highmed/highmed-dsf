package org.highmed.fhir.adapter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.hl7.fhir.r4.model.BaseResource;

import ca.uhn.fhir.context.FhirContext;

@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class AbstractXmlFhirAdapter<T extends BaseResource> extends AbstractFhirAdapter<T>
		implements MessageBodyReader<T>, MessageBodyWriter<T>
{
	public AbstractXmlFhirAdapter(FhirContext fhirContext, Class<T> resourceType)
	{
		super(resourceType, fhirContext.newXmlParser());
	}
}
