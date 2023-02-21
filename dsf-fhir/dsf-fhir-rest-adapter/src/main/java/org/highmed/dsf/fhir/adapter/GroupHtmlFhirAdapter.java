package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Group;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GroupHtmlFhirAdapter extends HtmlFhirAdapter<Group>
{
	public GroupHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Group.class);
	}
}
