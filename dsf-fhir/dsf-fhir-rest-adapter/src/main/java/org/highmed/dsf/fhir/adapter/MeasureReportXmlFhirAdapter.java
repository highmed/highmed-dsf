package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.MeasureReport;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MeasureReportXmlFhirAdapter extends XmlFhirAdapter<MeasureReport>
{
	public MeasureReportXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, MeasureReport.class);
	}
}
