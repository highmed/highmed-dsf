package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.ProcessEngine;
import org.highmed.dsf.bpe.webservice.ProcessService;
import org.highmed.dsf.bpe.webservice.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebserviceConfig
{
	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	private DaoConfig daoConfig;

	@Bean
	public ProcessService processService()
	{
		return new ProcessService(processEngine.getRuntimeService(), processEngine.getRepositoryService());
	}

	@Bean
	public StatusService statusService()
	{
		return new StatusService(daoConfig.dataSource());
	}
}
