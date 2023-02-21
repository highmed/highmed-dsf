package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Binary;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BinaryHtmlFhirAdapter extends HtmlFhirAdapter<Binary>
{
	public BinaryHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Binary.class);
	}
}
