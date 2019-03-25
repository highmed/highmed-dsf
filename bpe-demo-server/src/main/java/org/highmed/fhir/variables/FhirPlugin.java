package org.highmed.fhir.variables;

import java.util.Collections;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

public class FhirPlugin implements ProcessEnginePlugin
{
	private final DomainResourceSerializer domainResourceSerializer;

	public FhirPlugin(DomainResourceSerializer domainResourceSerializer)
	{
		this.domainResourceSerializer = domainResourceSerializer;
	}

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
		processEngineConfiguration.setCustomPreVariableSerializers(Collections.singletonList(domainResourceSerializer));
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration)
	{
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
	}
}
