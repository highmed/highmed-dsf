package org.highmed.dsf.fhir.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.QuestionnaireResponse;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class QuestionnaireResponseHtmlFhirAdapter extends HtmlFhirAdapter<QuestionnaireResponse>
{
	public QuestionnaireResponseHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, QuestionnaireResponse.class);
	}

	@Override
	public void writeTo(QuestionnaireResponse questionnaireResponse, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException
	{
		// TODO: show form to answer questionnaireResponse
		// if completed, values should not be editable but shown --> send no js to change

		super.writeTo(questionnaireResponse, type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}
}
