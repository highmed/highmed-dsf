package org.highmed.dsf.bpe.service;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.dsf.bpe.variables.SimpleCohortSizeResult;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CheckMultiMedicFeasibilityResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckMultiMedicFeasibilityResults.class);

	public CheckMultiMedicFeasibilityResults(FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		setTaskOutputs(execution);
	}

	private void setTaskOutputs(DelegateExecution execution)
	{
		List<SimpleCohortSizeResult> finalResult = (List<SimpleCohortSizeResult>) execution
				.getVariable(Constants.VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT);
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		OutputWrapper outputWrapper = new OutputWrapper(Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY);
		finalResult.forEach(simpleCohortSizeResult -> {
			outputWrapper.addKeyValue(
					Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY_VALUE_PREFIX_PARTICIPATING_MEDICS
							+ simpleCohortSizeResult.getCohortId(),
					String.valueOf(simpleCohortSizeResult.getParticipatingMedics()));
			outputWrapper.addKeyValue(
					Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY_VALUE_PREFIX_COHORT_SIZE
							+ simpleCohortSizeResult.getCohortId(),
					String.valueOf(simpleCohortSizeResult.getCohortSize()));
		});
		outputs.add(outputWrapper);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}
}
