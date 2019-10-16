package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class ProvenanceXmlFhirAdapter extends XmlFhirAdapter<Provenance>
{
	public ProvenanceXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Provenance.class);
	}
}
