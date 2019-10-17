package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.plugin.ParentPlugin;
import org.highmed.dsf.bpe.service.AfterPlugin;
import org.highmed.dsf.bpe.service.BeforePlugin;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParentConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Bean
	public ParentPlugin parentPlugin()
	{
		return new ParentPlugin();
	}

	@Bean
	public BeforePlugin beforePlugin()
	{
		return new BeforePlugin(clientProvider, taskHelper);
	}

	@Bean
	public AfterPlugin afterPlugin()
	{
		return new AfterPlugin(clientProvider, taskHelper);
	}
}
