package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class ValueSetJsonFhirAdapter extends JsonFhirAdapter<ValueSet>
{
	public ValueSetJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, ValueSet.class);
	}
}
