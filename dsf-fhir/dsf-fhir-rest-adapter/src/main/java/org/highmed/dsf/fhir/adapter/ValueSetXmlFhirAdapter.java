package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.ValueSet;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValueSetXmlFhirAdapter extends XmlFhirAdapter<ValueSet>
{
	public ValueSetXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, ValueSet.class);
	}
}
