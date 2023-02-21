package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.OperationOutcome;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class OperationOutcomeHtmlFhirAdapter extends HtmlFhirAdapter<OperationOutcome>
{
	public OperationOutcomeHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, OperationOutcome.class);
	}
}
