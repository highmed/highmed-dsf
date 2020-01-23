package org.highmed.dsf.bpe.spring.config;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.service.BpmnServiceDelegateValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig
{
	@Autowired
	private List<AbstractServiceDelegate> serviceBeans;

	@Autowired
	private ProcessEngine processEngine;

	@Bean
	protected BpmnServiceDelegateValidationService bpmnServiceDelegateValidationService()
	{
		return new BpmnServiceDelegateValidationService(processEngine, serviceBeans);
	}
}
