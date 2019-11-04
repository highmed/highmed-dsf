package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Binary;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class BinaryJsonFhirAdapter extends JsonFhirAdapter<Binary>
{
	public BinaryJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Binary.class);
	}
}
