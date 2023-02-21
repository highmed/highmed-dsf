package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Group;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GroupXmlFhirAdapter extends XmlFhirAdapter<Group>
{
	public GroupXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Group.class);
	}
}
