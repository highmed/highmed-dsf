package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class StructureDefinitionHtmlFhirAdapter extends HtmlFhirAdapter<StructureDefinition>
{
	public StructureDefinitionHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, StructureDefinition.class);
	}
}
