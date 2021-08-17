package org.highmed.dsf.bpe.camunda;

import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.highmed.dsf.bpe.delegate.DelegateProvider;

public class MultiVersionSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration
{
	public MultiVersionSpringProcessEngineConfiguration(DelegateProvider delegateProvider)
	{
		bpmnParseFactory = new MultiVersionBpmnParseFactory(delegateProvider);
	}

	@Override
	protected void initTelemetry()
	{
		// override to turn telemetry collection of
		// see also CamundaConfig
	}

	@Override
	public Data getTelemetryData()
	{
		// NPE fix after turning off telemetry collection
		// see also CamundaConfig
		return new Data(null, null);
	}
}
