package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.OperationOutcome;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OperationOutcomeJsonFhirAdapter extends JsonFhirAdapter<OperationOutcome>
{
	public OperationOutcomeJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, OperationOutcome.class);
	}
}
