package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckCohortSizeRequest implements JavaDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckCohortSizeRequest.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: executing ...", getClass().getName());

		/*
		// if requester != recipient then we are in a secondary, otherwise in the leading medic
		// do nothing if requester and recipient are the same, because check was already done
		if(task.getRequester() != task.getRestriction().getRecipient().get(0)))
		{
			// TODO: implement
			// TODO: check request task and research study
		}
		*/

		// TODO: rejection handling
	}
}
