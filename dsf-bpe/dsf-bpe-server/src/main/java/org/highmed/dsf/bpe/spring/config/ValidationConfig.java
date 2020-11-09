package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.ProcessEngine;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.service.BpmnServiceDelegateValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig
{
	@Autowired
	private DelegateProvider delegateProvider;

	@Autowired
	private ProcessEngine processEngine;

	@Bean
	protected BpmnServiceDelegateValidationService bpmnServiceDelegateValidationService()
	{
		return new BpmnServiceDelegateValidationService(processEngine, delegateProvider);
	}
}
