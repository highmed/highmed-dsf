package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.MeasureReport;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class MeasureReportJsonFhirAdapter extends JsonFhirAdapter<MeasureReport>
{
	public MeasureReportJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, MeasureReport.class);
	}
}
