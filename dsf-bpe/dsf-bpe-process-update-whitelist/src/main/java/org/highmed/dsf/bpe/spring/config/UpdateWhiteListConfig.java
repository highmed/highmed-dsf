package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.plugin.UpdateWhiteListPlugin;
import org.highmed.dsf.bpe.service.UpdateWhiteList;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateWhiteListConfig
{
	@Autowired
	private WebserviceClientProvider clientProvider;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Bean
	public ProcessEnginePlugin updateWhiteListPlugin()
	{
		return new UpdateWhiteListPlugin();
	}

	@Bean
	public UpdateWhiteList updateWhiteList()
	{
		return new UpdateWhiteList(clientProvider, organizationProvider);
	}
}
