package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.DocumentReference;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DocumentReferenceXmlFhirAdapter extends XmlFhirAdapter<DocumentReference>
{
	public DocumentReferenceXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, DocumentReference.class);
	}
}
