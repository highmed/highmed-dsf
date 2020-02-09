package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Group;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class GroupHtmlFhirAdapter extends HtmlFhirAdapter<Group>
{
	public GroupHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Group.class);
	}
}
