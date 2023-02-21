package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Library;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LibraryHtmlFhirAdapter extends HtmlFhirAdapter<Library>
{
	public LibraryHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Library.class);
	}
}
