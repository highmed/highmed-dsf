package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.plugin.ChildPlugin;
import org.highmed.dsf.bpe.service.ExecutePlugin;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChildConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Bean
	public ChildPlugin childPlugin()
	{
		return new ChildPlugin();
	}

	@Bean
	public ExecutePlugin executePlugin()
	{
		return new ExecutePlugin(clientProvider, taskHelper);
	}
}
