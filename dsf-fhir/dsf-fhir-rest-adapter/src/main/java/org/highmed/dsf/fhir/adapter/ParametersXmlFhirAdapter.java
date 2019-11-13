package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Parameters;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class ParametersXmlFhirAdapter extends XmlFhirAdapter<Parameters>
{
	public ParametersXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Parameters.class);
	}
}
