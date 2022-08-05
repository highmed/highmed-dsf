package org.highmed.dsf.fhir.task;

import java.util.Optional;

import org.highmed.dsf.fhir.subscription.AbstractExistingResourceLoader;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;

public class ExistingTaskLoader extends AbstractExistingResourceLoader<Task>
{
	public ExistingTaskLoader(LastEventTimeIo lastEventTimeIo, ResourceHandler<Task> handler,
			FhirWebserviceClient webserviceClient)
	{
		super(lastEventTimeIo, handler, webserviceClient, "Task", Task.class);
	}

	@Override
	protected Optional<Task> castExistingResource(Resource resource)
	{
		if (resource instanceof Task)
			return Optional.of((Task) resource);
		else
			return Optional.empty();
	}
}
