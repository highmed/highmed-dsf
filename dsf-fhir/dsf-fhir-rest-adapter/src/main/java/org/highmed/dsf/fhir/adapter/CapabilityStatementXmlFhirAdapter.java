package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.CapabilityStatement;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class CapabilityStatementXmlFhirAdapter extends XmlFhirAdapter<CapabilityStatement>
{
	public CapabilityStatementXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CapabilityStatement.class);
	}
}
