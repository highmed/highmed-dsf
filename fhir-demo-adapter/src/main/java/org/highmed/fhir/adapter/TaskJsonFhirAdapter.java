package org.highmed.fhir.adapter;

import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class TaskJsonFhirAdapter extends JsonFhirAdapter<Task>
{
	public TaskJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Task.class);
	}
}
