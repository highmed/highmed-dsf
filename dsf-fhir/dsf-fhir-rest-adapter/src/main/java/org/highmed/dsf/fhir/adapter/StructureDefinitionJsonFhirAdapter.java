package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class StructureDefinitionJsonFhirAdapter extends JsonFhirAdapter<StructureDefinition>
{
	public StructureDefinitionJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, StructureDefinition.class);
	}
}
