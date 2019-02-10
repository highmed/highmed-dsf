package org.highmed.fhir.adapter;

import org.hl7.fhir.r4.model.CapabilityStatement;

import ca.uhn.fhir.context.FhirContext;

public class CapabilityStatementXmlFhirAdapter extends XmlFhirAdapter<CapabilityStatement>
{
	public CapabilityStatementXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CapabilityStatement.class);
	}
}
