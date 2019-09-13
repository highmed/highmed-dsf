package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Group;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class GroupJsonFhirAdapter extends JsonFhirAdapter<Group>
{
	public GroupJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Group.class);
	}
}
