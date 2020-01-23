package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.message.SendRequest;
import org.highmed.dsf.bpe.plugin.UpdateResourcesPlugin;
import org.highmed.dsf.bpe.service.CheckRequest;
import org.highmed.dsf.bpe.service.SelectResourceAndTargets;
import org.highmed.dsf.bpe.service.UpdateResources;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class UpdateResourcesConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private FhirContext fhirContext;

	@Bean
	public ProcessEnginePlugin updateResourcesPlugin()
	{
		return new UpdateResourcesPlugin();
	}

	@Bean
	public SendRequest sendRequest()
	{
		return new SendRequest(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Bean
	public SelectResourceAndTargets selectUpdateResourcesTargets()
	{
		return new SelectResourceAndTargets(clientProvider, taskHelper, organizationProvider);
	}

	@Bean
	public UpdateResources updateResources()
	{
		return new UpdateResources(clientProvider, taskHelper, fhirContext);
	}

	@Bean
	public CheckRequest checkRequest()
	{
		return new CheckRequest(clientProvider, taskHelper);
	}
}
