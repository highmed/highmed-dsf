package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.ActivityDefinition;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ActivityDefinitionXmlFhirAdapter extends XmlFhirAdapter<ActivityDefinition>
{
	public ActivityDefinitionXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, ActivityDefinition.class);
	}
}
