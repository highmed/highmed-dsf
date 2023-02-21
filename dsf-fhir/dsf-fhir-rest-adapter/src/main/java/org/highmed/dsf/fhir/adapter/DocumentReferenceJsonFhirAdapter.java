package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.DocumentReference;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DocumentReferenceJsonFhirAdapter extends JsonFhirAdapter<DocumentReference>
{
	public DocumentReferenceJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, DocumentReference.class);
	}
}
