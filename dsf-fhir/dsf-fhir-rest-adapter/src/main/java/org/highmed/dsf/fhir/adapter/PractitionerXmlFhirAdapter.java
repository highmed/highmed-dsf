package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PractitionerXmlFhirAdapter extends XmlFhirAdapter<Practitioner>
{
	public PractitionerXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Practitioner.class);
	}
}
