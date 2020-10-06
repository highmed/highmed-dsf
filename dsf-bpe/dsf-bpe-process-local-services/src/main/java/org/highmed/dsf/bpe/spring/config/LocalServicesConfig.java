package org.highmed.dsf.bpe.spring.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.highmed.dsf.bpe.plugin.LocalServicesPlugin;
import org.highmed.dsf.bpe.service.ExtractInputValues;
import org.highmed.dsf.bpe.service.StoreResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalServicesConfig
{
	@Autowired
	private FhirWebserviceClientProvider fhirClientProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Bean
	public ProcessEnginePlugin localServicesPlugin()
	{
		return new LocalServicesPlugin();
	}

	@Bean
	public ExtractInputValues extractInputValues()
	{
		return new ExtractInputValues(fhirClientProvider, taskHelper);
	}

	@Bean
	public StoreResult storeResult()
	{
		return new StoreResult(fhirClientProvider, taskHelper);
	}
}
