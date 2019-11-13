package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class BundleXmlFhirAdapter extends XmlFhirAdapter<Bundle>
{
	public BundleXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Bundle.class);
	}
}
