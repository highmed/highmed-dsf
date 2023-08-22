package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.ActivityDefinition;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class ActivityDefinitionHtmlFhirAdapter extends HtmlFhirAdapter<ActivityDefinition>
{
	public ActivityDefinitionHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, ActivityDefinition.class);
	}
}
