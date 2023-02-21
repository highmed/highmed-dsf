package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.NamingSystem;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class NamingSystemXmlFhirAdapter extends XmlFhirAdapter<NamingSystem>
{
	public NamingSystemXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, NamingSystem.class);
	}
}
