package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.CapabilityStatement;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CapabilityStatementJsonFhirAdapter extends JsonFhirAdapter<CapabilityStatement>
{
	public CapabilityStatementJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CapabilityStatement.class);
	}
}
