package org.highmed.bpe.spring.config;

import org.highmed.bpe.plugin.UpdateWhiteListPlugin;
import org.highmed.bpe.service.UpdateWhiteList;
import org.highmed.fhir.client.WebserviceClientProvider;
import org.highmed.fhir.organization.OrganizationProvider;
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
	public UpdateWhiteListPlugin updateWhiteListPlugin()
	{
		return new UpdateWhiteListPlugin();
	}

	@Bean
	public UpdateWhiteList updateWhiteList()
	{
		return new UpdateWhiteList(clientProvider, organizationProvider);
	}
}
