package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.DocumentReference;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DocumentReferenceHtmlFhirAdapter extends HtmlFhirAdapter<DocumentReference>
{
	public DocumentReferenceHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, DocumentReference.class);
	}
}
