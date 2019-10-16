package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Parameters;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class ParametersJsonFhirAdapter extends JsonFhirAdapter<Parameters>
{
	public ParametersJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Parameters.class);
	}
}
