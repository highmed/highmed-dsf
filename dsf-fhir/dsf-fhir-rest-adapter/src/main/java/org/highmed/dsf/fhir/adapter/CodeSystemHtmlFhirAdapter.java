package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class CodeSystemHtmlFhirAdapter extends HtmlFhirAdapter<CodeSystem>
{
	public CodeSystemHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, CodeSystem.class);
	}
}
