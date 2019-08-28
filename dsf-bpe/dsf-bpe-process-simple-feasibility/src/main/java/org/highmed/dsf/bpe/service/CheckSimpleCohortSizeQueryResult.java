package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSimpleCohortSizeQueryResult extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckSimpleCohortSizeQueryResult.class);

	public CheckSimpleCohortSizeQueryResult(WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Integer result = (Integer) execution.getVariable(Constants.VARIABLE_QUERY_RESULT);

		// TODO: implement check for result
		// TODO: implement check for ...
	}

	private void stopInstance(String reason)
	{
		logger.error("Result review failed, reason {}", reason);
		throw new RuntimeException("Result review failed, reason " + reason);
	}
}
