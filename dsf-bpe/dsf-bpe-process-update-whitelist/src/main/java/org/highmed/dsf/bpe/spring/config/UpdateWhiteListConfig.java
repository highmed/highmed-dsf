package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.plugin.UpdateWhiteListPlugin;
import org.highmed.dsf.bpe.service.UpdateWhiteList;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateWhiteListConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Bean
	public ProcessEnginePlugin updateWhiteListPlugin()
	{
		return new UpdateWhiteListPlugin();
	}

	@Bean
	public UpdateWhiteList updateWhiteList()
	{
		return new UpdateWhiteList(organizationProvider, clientProvider, taskHelper);
	}
}
