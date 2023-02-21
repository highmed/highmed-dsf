package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class LocationHtmlFhirAdapter extends HtmlFhirAdapter<Location>
{
	public LocationHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Location.class);
	}
}
