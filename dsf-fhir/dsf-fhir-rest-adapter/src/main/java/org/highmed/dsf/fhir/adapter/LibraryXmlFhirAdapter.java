package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Library;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LibraryXmlFhirAdapter extends XmlFhirAdapter<Library>
{
	public LibraryXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Library.class);
	}
}
