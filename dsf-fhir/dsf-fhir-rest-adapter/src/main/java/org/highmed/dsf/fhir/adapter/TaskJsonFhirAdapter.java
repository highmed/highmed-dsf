package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class TaskJsonFhirAdapter extends JsonFhirAdapter<Task>
{
	public TaskJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Task.class);
	}
}
