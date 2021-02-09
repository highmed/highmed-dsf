package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Library;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class LibraryJsonFhirAdapter extends JsonFhirAdapter<Library>
{
	public LibraryJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Library.class);
	}
}
