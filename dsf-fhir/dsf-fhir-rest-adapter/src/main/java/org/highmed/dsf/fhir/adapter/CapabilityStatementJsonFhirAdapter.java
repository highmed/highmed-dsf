package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.CapabilityStatement;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class CapabilityStatementJsonFhirAdapter extends JsonFhirAdapter<CapabilityStatement>
{
	public CapabilityStatementJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CapabilityStatement.class);
	}
}
