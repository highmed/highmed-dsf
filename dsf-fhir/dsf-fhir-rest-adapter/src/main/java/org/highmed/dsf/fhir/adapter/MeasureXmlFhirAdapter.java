package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Measure;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MeasureXmlFhirAdapter extends XmlFhirAdapter<Measure>
{
	public MeasureXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Measure.class);
	}
}
