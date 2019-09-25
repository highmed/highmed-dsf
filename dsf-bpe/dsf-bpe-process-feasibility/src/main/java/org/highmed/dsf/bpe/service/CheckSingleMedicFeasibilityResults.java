package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSingleMedicFeasibilityResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckSingleMedicFeasibilityResults.class);

	public CheckSingleMedicFeasibilityResults(FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		// TODO
	}
}
