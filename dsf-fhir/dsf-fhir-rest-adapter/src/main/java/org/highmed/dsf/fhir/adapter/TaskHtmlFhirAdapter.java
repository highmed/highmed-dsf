package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TaskHtmlFhirAdapter extends HtmlFhirAdapter<Task>
{
	public TaskHtmlFhirAdapter(FhirContext fhirContext, ServerBaseProvider serverBaseProvider)
	{
		super(fhirContext, serverBaseProvider, Task.class);
	}
}
