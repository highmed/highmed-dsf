package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class BarService implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(BarService.class);

	public BarService()
	{
		logger.info("TestService() " + System.identityHashCode(this));
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		logger.info("afterPropertiesSet() " + System.identityHashCode(this));
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.info("Process instance ID {}, business key {}", execution.getProcessInstanceId(),
				execution.getBusinessKey());
		logger.info("Variables: {}", execution.getVariables());
		logger.info("Bar Service ... " + System.identityHashCode(this));
	}
}
