package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class BundleJsonFhirAdapter extends JsonFhirAdapter<Bundle>
{
	public BundleJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Bundle.class);
	}
}
