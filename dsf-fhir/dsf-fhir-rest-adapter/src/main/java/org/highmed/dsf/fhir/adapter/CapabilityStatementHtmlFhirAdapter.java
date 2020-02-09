package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.CapabilityStatement;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class CapabilityStatementHtmlFhirAdapter extends HtmlFhirAdapter<CapabilityStatement>
{
	public CapabilityStatementHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, CapabilityStatement.class);
	}
}
