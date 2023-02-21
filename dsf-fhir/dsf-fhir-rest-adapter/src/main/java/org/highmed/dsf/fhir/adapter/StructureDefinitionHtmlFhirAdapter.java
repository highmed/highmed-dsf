package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class StructureDefinitionHtmlFhirAdapter extends HtmlFhirAdapter<StructureDefinition>
{
	public StructureDefinitionHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, StructureDefinition.class);
	}
}
