package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.DocumentReference;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class DocumentReferenceHtmlFhirAdapter extends HtmlFhirAdapter<DocumentReference>
{
	public DocumentReferenceHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, DocumentReference.class);
	}
}
