package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Provenance;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ProvenanceHtmlFhirAdapter extends HtmlFhirAdapter<Provenance>
{
	public ProvenanceHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Provenance.class);
	}
}
