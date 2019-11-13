package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class ProvenanceJsonFhirAdapter extends JsonFhirAdapter<Provenance>
{
	public ProvenanceJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Provenance.class);
	}
}
