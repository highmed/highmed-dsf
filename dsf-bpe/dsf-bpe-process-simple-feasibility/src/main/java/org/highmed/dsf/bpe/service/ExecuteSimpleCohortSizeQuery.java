package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteSimpleCohortSizeQuery extends AbstractServiceDelegate
{

	private static final Logger logger = LoggerFactory.getLogger(ExecuteSimpleCohortSizeQuery.class);

	public ExecuteSimpleCohortSizeQuery(WebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		// TODO: implement openehr client and execute query
		// TODO: change to multiinstance for multiple cohorts of one research study

		// TODO: is mock result, remove
		execution.setVariable(Constants.VARIABLE_QUERY_RESULT, 10);
	}
}
