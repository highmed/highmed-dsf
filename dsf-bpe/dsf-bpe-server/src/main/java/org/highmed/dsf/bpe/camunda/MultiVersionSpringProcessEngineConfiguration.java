package org.highmed.dsf.bpe.camunda;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.highmed.dsf.bpe.delegate.DelegateProvider;

public class MultiVersionSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration
{
	public MultiVersionSpringProcessEngineConfiguration(DelegateProvider delegateProvider)
	{
		bpmnParseFactory = new MultiVersionBpmnParseFactory(delegateProvider);
	}
}
