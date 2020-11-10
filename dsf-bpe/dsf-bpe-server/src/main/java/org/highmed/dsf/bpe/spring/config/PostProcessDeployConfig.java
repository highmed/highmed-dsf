package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.ProcessEngine;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.plugin.ProcessPluginProvider;
import org.highmed.dsf.bpe.service.BpmnProcessStateChangeService;
import org.highmed.dsf.bpe.service.BpmnProcessStateChangeServiceImpl;
import org.highmed.dsf.bpe.service.BpmnServiceDelegateValidationService;
import org.highmed.dsf.bpe.service.BpmnServiceDelegateValidationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class PostProcessDeployConfig
{
	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	private DelegateProvider delegateProvider;

	@Autowired
	private ProcessPluginProvider processPluginProvider;

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		bpmnProcessStateChangeService().suspendOrActivateProcesses();
		bpmnServiceDelegateValidationService().validateModels();
	}

	@Bean
	public BpmnServiceDelegateValidationService bpmnServiceDelegateValidationService()
	{
		return new BpmnServiceDelegateValidationServiceImpl(processEngine, delegateProvider);
	}

	@Bean
	public BpmnProcessStateChangeService bpmnProcessStateChangeService()
	{
		return new BpmnProcessStateChangeServiceImpl(processEngine.getRepositoryService(), processPluginProvider);
	}
}
