package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class LocationXmlFhirAdapter extends XmlFhirAdapter<Location>
{
	public LocationXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Location.class);
	}
}
