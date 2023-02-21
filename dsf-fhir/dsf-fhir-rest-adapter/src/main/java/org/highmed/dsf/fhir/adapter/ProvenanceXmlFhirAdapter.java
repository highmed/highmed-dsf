package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ProvenanceXmlFhirAdapter extends XmlFhirAdapter<Provenance>
{
	public ProvenanceXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Provenance.class);
	}
}
