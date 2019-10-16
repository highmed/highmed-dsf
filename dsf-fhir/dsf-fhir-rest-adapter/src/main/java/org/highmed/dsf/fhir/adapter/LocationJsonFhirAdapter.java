package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class LocationJsonFhirAdapter extends JsonFhirAdapter<Location>
{
	public LocationJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Location.class);
	}
}
