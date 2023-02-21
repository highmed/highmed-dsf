package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CodeSystemXmlFhirAdapter extends XmlFhirAdapter<CodeSystem>
{
	public CodeSystemXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CodeSystem.class);
	}
}
