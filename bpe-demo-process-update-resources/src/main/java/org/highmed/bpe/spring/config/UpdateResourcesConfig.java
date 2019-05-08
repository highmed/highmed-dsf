package org.highmed.bpe.spring.config;

import org.highmed.bpe.message.SendRequest;
import org.highmed.bpe.plugin.UpdateResourcesPlugin;
import org.highmed.bpe.service.SelectUpdateResourcesTargets;
import org.highmed.bpe.service.UpdateResources;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateResourcesConfig
{
	@Autowired
	private WebserviceClientProvider clientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Bean
	public UpdateResourcesPlugin updateResourcesPlugin()
	{
		return new UpdateResourcesPlugin();
	}

	@Bean
	public SendRequest sendRequest()
	{
		return new SendRequest(organizationProvider, clientProvider);
	}

	@Bean
	public SelectUpdateResourcesTargets selectUpdateResourcesTargets()
	{
		return new SelectUpdateResourcesTargets(organizationProvider);
	}

	@Bean
	public UpdateResources updateResources()
	{
		return new UpdateResources(clientProvider, taskHelper);
	}
}
