package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Binary;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class BinaryHtmlFhirAdapter extends HtmlFhirAdapter<Binary>
{
	public BinaryHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Binary.class);
	}
}
