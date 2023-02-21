package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class CodeSystemJsonFhirAdapter extends JsonFhirAdapter<CodeSystem>
{
	public CodeSystemJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CodeSystem.class);
	}
}
