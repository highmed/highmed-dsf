package org.highmed.dsf.fhir.adapter;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Group;

import javax.ws.rs.ext.Provider;

@Provider
public class GroupJsonFhirAdapter extends JsonFhirAdapter<Group>
{
	public GroupJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Group.class);
	}
}
