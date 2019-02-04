package org.highmed.fhir.werbservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Path;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Path(TaskProvider.PATH)
public class TaskProvider extends AbstractJaxRsResourceProvider<Task>
{
	public static final String PATH = "Task";

	private static final Logger logger = LoggerFactory.getLogger(TaskProvider.class);

	private final Map<String, List<Task>> tasks = new HashMap<>();
	private final String serverBase;

	public TaskProvider(FhirContext fhirContext, String serverBase)
	{
		super(fhirContext, TaskProvider.class);

		this.serverBase = serverBase;
	}

	@Override
	public Class<Task> getResourceType()
	{
		return Task.class;
	}

	@Create
	public MethodOutcome create(@ResourceParam final Task task, @ConditionalUrlParam String theConditional)
			throws Exception
	{
		logger.debug("create ...");

		task.setIdElement(createId());
		tasks.merge(task.getIdElement().getIdPart(), new ArrayList<Task>(Collections.singleton(task)), (l1, l2) ->
		{
			l1.addAll(l2);
			return l1;
		});

		final MethodOutcome result = new MethodOutcome().setCreated(true);
		result.setResource(task);
		result.setId(task.getIdElement());

		return result;
	}

	private IdType createId()
	{
		return new IdType(serverBase, Task.class.getAnnotation(ResourceDef.class).name(), UUID.randomUUID().toString(),
				"1");
	}

	@Read
	public Task read(@IdParam final IdType id)
	{
		logger.debug("read {} ...", id.getValue());

		if (id.getBaseUrl() != null && !serverBase.equals(id.getBaseUrl()))
			throw new ResourceNotFoundException(id);

		if (tasks.containsKey(id.getIdPart()))
		{
			List<Task> tasks = this.tasks.get(id.getIdPart());
			return tasks.get(tasks.size() - 1);
		}
		else
			throw new ResourceNotFoundException(id);
	}

	@Read(version = true)
	public Task readVersion(@IdParam final IdType id)
	{
		logger.debug("readVersion {} ...", id.getValue());

		if (id.getBaseUrl() != null && !serverBase.equals(id.getBaseUrl()))
			throw new ResourceNotFoundException(id);

		return tasks.getOrDefault(id.getIdPart(), Collections.emptyList()).stream()
				.filter(t -> t.getIdElement().getVersionIdPart().contentEquals(id.getVersionIdPart())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException(id));
	}
}
