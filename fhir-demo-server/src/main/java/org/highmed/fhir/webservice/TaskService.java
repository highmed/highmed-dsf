package org.highmed.fhir.webservice;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.highmed.fhir.dao.TaskDao;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UriType;

import ca.uhn.fhir.rest.api.Constants;

@Path(TaskService.RESOURCE_TYPE_NAME)
public class TaskService extends AbstractService<TaskDao, Task>
{
	public static final String RESOURCE_TYPE_NAME = "Task";

	public TaskService(String serverBase, TaskDao taskDao)
	{
		super(serverBase, RESOURCE_TYPE_NAME, taskDao);
	}

	@GET
	@Produces({ Constants.CT_FHIR_JSON, Constants.CT_FHIR_JSON_NEW, MediaType.APPLICATION_JSON, Constants.CT_FHIR_XML,
			Constants.CT_FHIR_XML_NEW, MediaType.APPLICATION_XML })
	public Response search(@QueryParam("requester") String requester, @QueryParam("_format") String format,
			@Context UriInfo uri)
	{
		List<Task> tasks = handleSql(() -> getDao().getTaskBy(new IdType(requester)));

		UriBuilder bundleUri = uri.getAbsolutePathBuilder();
		if (requester != null)
			bundleUri = bundleUri.replaceQueryParam("requester", requester);
		if (format != null)
			bundleUri = bundleUri.replaceQueryParam("_format", format);

		return response(Status.OK, createSearchSet(tasks, bundleUri.build()), toSpecialMimeType(format)).build();
	}

	private Bundle createSearchSet(List<? extends DomainResource> resources, URI uri)
	{
		Bundle bundle = new Bundle();
		bundle.setId(UUID.randomUUID().toString());
		bundle.getMeta().setLastUpdated(getLatest(resources));
		bundle.setType(BundleType.SEARCHSET);
		bundle.setEntry(
				resources.stream().map(r -> new BundleEntryComponent().setResource(r).setFullUrl(toFullId(r.getId())))
						.collect(Collectors.toList()));
		bundle.setTotal(resources.size());
		bundle.addLink().setRelation("self").setUrlElement(new UriType(uri));
		return bundle;
	}

	private Date getLatest(List<? extends DomainResource> resources)
	{
		return resources.stream().map(r -> r.getMeta().getLastUpdated()).sorted().findFirst().orElse(new Date());
	}
}
