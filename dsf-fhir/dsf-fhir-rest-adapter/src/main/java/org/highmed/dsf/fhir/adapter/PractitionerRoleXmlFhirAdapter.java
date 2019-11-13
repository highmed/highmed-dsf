package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class PractitionerRoleXmlFhirAdapter extends XmlFhirAdapter<PractitionerRole>
{
	public PractitionerRoleXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, PractitionerRole.class);
	}
}
