package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CodeSystemJsonFhirAdapter extends JsonFhirAdapter<CodeSystem>
{
	public CodeSystemJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CodeSystem.class);
	}
}
