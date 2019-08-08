package org.highmed.dsf.bpe.message;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendCohortSizeResultToMedic implements JavaDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(SendCohortSizeResultToMedic.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: executing ...", getClass().getName());

		// TODO: implement
	}
}
