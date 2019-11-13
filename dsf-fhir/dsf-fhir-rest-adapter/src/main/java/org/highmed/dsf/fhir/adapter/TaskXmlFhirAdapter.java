package org.highmed.dsf.fhir.adapter;

import javax.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

@Provider
public class TaskXmlFhirAdapter extends XmlFhirAdapter<Task>
{
	public TaskXmlFhirAdapter(FhirContext fhirContext)
	{
		super(fhirContext, Task.class);
	}
}
