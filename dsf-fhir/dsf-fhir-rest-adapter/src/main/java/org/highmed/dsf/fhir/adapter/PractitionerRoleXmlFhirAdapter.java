package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.PractitionerRole;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PractitionerRoleXmlFhirAdapter extends XmlFhirAdapter<PractitionerRole>
{
	public PractitionerRoleXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, PractitionerRole.class);
	}
}
