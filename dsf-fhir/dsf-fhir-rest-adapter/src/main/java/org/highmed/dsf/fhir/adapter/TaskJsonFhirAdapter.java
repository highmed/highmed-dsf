package org.highmed.dsf.fhir.adapter;

import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TaskJsonFhirAdapter extends JsonFhirAdapter<Task>
{
	public TaskJsonFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Task.class);
	}
}
