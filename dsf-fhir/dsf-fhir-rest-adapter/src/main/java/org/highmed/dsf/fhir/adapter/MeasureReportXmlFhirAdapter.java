package org.highmed.dsf.fhir.adapter;

import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.MeasureReport;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class MeasureReportXmlFhirAdapter extends XmlFhirAdapter<MeasureReport>
{
	public MeasureReportXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, MeasureReport.class);
	}
}
