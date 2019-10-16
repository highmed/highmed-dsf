package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.HealthcareService;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class HealthcareServiceXmlFhirAdapter extends XmlFhirAdapter<HealthcareService>
{
	public HealthcareServiceXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, HealthcareService.class);
	}
}
