package org.highmed.fhir.adapter;

import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class TaskXmlFhirAdapter extends XmlFhirAdapter<Task>
{
	public TaskXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Task.class);
	}
}
