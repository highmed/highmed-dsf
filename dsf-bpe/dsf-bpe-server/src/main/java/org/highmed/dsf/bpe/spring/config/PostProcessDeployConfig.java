package org.highmed.dsf.bpe.spring.config;

import java.util.List;
import java.util.stream.Stream;

import org.camunda.bpm.engine.ProcessEngine;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.plugin.ProcessPluginProvider;
import org.highmed.dsf.bpe.process.BpmnFileAndModel;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.highmed.dsf.bpe.process.ProcessStateChangeOutcome;
import org.highmed.dsf.bpe.service.BpmnProcessStateChangeService;
import org.highmed.dsf.bpe.service.BpmnProcessStateChangeServiceImpl;
import org.highmed.dsf.bpe.service.BpmnServiceDelegateValidationService;
import org.highmed.dsf.bpe.service.BpmnServiceDelegateValidationServiceImpl;
import org.highmed.dsf.bpe.service.FhirResourceHandler;
import org.highmed.dsf.bpe.service.FhirResourceHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class PostProcessDeployConfig
{
	@Autowired
	private PropertiesConfig propertiesConfig;

	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	private DelegateProvider delegateProvider;

	@Autowired
	private ProcessPluginProvider processPluginProvider;

	@Autowired
	private FhirConfig fhirConfig;

	@Autowired
	private DaoConfig daoConfig;

	@EventListener({ ContextRefreshedEvent.class })
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		Stream<BpmnFileAndModel> models = processPluginProvider.getDefinitions().stream()
				.flatMap(def -> def.getAndValidateModels().stream());

		List<ProcessStateChangeOutcome> changes = bpmnProcessStateChangeService()
				.deploySuspendOrActivateProcesses(models);

		bpmnServiceDelegateValidationService().validateModels();

		fhirResourceHandler().applyStateChangesAndStoreNewResourcesInDb(
				processPluginProvider.getDefinitionByProcessKeyAndVersion(), changes);

		processPluginProvider.onProcessesDeployed(changes);
	}

	@Bean
	public BpmnServiceDelegateValidationService bpmnServiceDelegateValidationService()
	{
		return new BpmnServiceDelegateValidationServiceImpl(processEngine, delegateProvider);
	}

	@Bean
	public BpmnProcessStateChangeService bpmnProcessStateChangeService()
	{
		return new BpmnProcessStateChangeServiceImpl(processEngine.getRepositoryService(), daoConfig.processStateDao(),
				processPluginProvider, ProcessKeyAndVersion.fromStrings(propertiesConfig.getProcessExcluded()),
				ProcessKeyAndVersion.fromStrings(propertiesConfig.getProcessRetired()));
	}

	@Bean
	public FhirResourceHandler fhirResourceHandler()
	{
		return new FhirResourceHandlerImpl(fhirConfig.clientProvider().getLocalWebserviceClient(),
				daoConfig.processPluginResourcesDao(), fhirConfig.fhirContext(),
				propertiesConfig.getFhirServerRequestMaxRetries(), propertiesConfig.getFhirServerRetryDelayMillis(),
				processPluginProvider.getResouceProvidersByDpendencyNameAndVersion());
	}
}
