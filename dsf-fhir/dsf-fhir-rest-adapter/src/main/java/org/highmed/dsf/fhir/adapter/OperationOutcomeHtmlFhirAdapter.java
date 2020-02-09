package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.OperationOutcome;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class OperationOutcomeHtmlFhirAdapter extends HtmlFhirAdapter<OperationOutcome>
{
	public OperationOutcomeHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, OperationOutcome.class);
	}
}
