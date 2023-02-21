package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.BaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

@Consumes({ Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_JSON, MediaType.APPLICATION_JSON })
@Produces({ Constants.CT_FHIR_JSON_NEW, Constants.CT_FHIR_JSON, MediaType.APPLICATION_JSON })
public class JsonFhirAdapter<T extends BaseResource> extends AbstractFhirAdapter<T>
		implements MessageBodyReader<T>, MessageBodyWriter<T>
{
	protected JsonFhirAdapter(FhirContext fhirContext, Class<T> resourceType)
	{
		/* Parsers are not guaranteed to be thread safe */
		super(resourceType, fhirContext::newJsonParser);
	}
}
