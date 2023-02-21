package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValueSetHtmlFhirAdapter extends HtmlFhirAdapter<ValueSet>
{
	public ValueSetHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, ValueSet.class);
	}
}
