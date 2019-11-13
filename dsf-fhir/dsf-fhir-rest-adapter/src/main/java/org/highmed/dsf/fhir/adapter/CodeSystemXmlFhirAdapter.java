package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class CodeSystemXmlFhirAdapter extends XmlFhirAdapter<CodeSystem>
{
	public CodeSystemXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, CodeSystem.class);
	}
}
