package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckCohortSizeResult implements JavaDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckCohortSizeResult.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: executing ...", getClass().getName());

		// TODO: implement
		// TODO: rejection handling
	}
}
