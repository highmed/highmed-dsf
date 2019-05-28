package org.highmed.bpe.spring.config;

import org.camunda.bpm.engine.ProcessEngine;
import org.highmed.bpe.werbservice.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfig
{
	@Autowired
	private ProcessEngine processEngine;

	@Bean
	public ProcessService processService()
	{
		return new ProcessService(processEngine.getRuntimeService(), processEngine.getRepositoryService());
	}
}
