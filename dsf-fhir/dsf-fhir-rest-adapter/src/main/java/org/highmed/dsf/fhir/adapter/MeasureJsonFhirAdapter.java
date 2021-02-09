package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Measure;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class MeasureJsonFhirAdapter extends JsonFhirAdapter<Measure>
{
	public MeasureJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Measure.class);
	}
}
