package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.OperationOutcome;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class OperationOutcomeXmlFhirAdapter extends XmlFhirAdapter<OperationOutcome>
{
	public OperationOutcomeXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, OperationOutcome.class);
	}
}
