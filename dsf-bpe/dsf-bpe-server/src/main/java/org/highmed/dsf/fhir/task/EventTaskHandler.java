package org.highmed.dsf.fhir.task;

import java.util.Optional;

import org.highmed.dsf.fhir.subscription.AbstractEventResourceHandler;
import org.highmed.dsf.fhir.websocket.LastEventTimeIo;
import org.highmed.dsf.fhir.websocket.ResourceHandler;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;

public class EventTaskHandler extends AbstractEventResourceHandler<Task>
{
	public EventTaskHandler(LastEventTimeIo lastEventTimeIo, ResourceHandler<Task> handler)
	{
		super(lastEventTimeIo, handler);
	}

	@Override
	protected Optional<Task> castResource(Resource resource)
	{
		if (resource instanceof Task)
			return Optional.of((Task) resource);
		else
			return Optional.empty();
	}
}
