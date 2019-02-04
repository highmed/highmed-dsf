package org.highmed.fhir.werbservice;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsConformanceProvider;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.IResourceProvider;

@Path("")
@Produces({ MediaType.APPLICATION_JSON, Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML })
public class ConformanceProvider extends AbstractJaxRsConformanceProvider
{
	private static final String SERVER_VERSION = "0.1.0-SNAPSHOT";
	private static final String SERVER_DESCRIPTION = "HiGHmed FHIR Demo Server";
	private static final String SERVER_NAME = "HiGHmed FHIR";

	private final String serverBase;
	private final TaskProvider taskProvider;

	public ConformanceProvider(FhirContext fhirContext, String serverBase, TaskProvider taskProvider)
	{
		super(fhirContext, SERVER_DESCRIPTION, SERVER_NAME, SERVER_VERSION);

		this.serverBase = serverBase;
		this.taskProvider = taskProvider;
	}

	@Override
	protected ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider> getProviders()
	{
		ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider> map = new ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider>();
		map.put(ConformanceProvider.class, this);
		map.put(TaskProvider.class, taskProvider);
		return map;
	}

	@Override
	public String getBaseForServer()
	{
		return serverBase;
	}
}
