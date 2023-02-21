package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LocationXmlFhirAdapter extends XmlFhirAdapter<Location>
{
	public LocationXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Location.class);
	}
}
