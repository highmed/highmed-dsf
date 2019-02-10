package org.highmed.fhir.adapter;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.BaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;

@Provider
@Consumes({ Constants.CT_FHIR_XML_NEW, Constants.CT_FHIR_XML, MediaType.APPLICATION_XML })
@Produces({ Constants.CT_FHIR_XML_NEW, Constants.CT_FHIR_XML, MediaType.APPLICATION_XML })
public class XmlFhirAdapter<T extends BaseResource> extends AbstractFhirAdapter<T>
		implements MessageBodyReader<T>, MessageBodyWriter<T>
{
	protected XmlFhirAdapter(FhirContext fhirContext, Class<T> resourceType)
	{
		super(resourceType, fhirContext.newXmlParser());
	}
}
