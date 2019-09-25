package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;
import static org.highmed.dsf.bpe.Constants.SIMPLE_FEASIBILITY_QUERY_PREFIX;

import java.util.ArrayList;
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
		List<SimpleCohortSizeResult> finalResult = (List<SimpleCohortSizeResult>) execution
				.getVariable(Constants.VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT);
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		checkResults(finalResult, outputs);
		setTaskOutputsSuccessful(finalResult, outputs);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private void checkResults(List<SimpleCohortSizeResult> finalResult, List<OutputWrapper> outputs)
	{
		List<SimpleCohortSizeResult> toRemove = new ArrayList<>();

		finalResult.forEach(result -> {
			if (result.getParticipatingMedics() < MIN_PARTICIPATING_MEDICS)
			{
				toRemove.add(result);
				setTaskOutputErroneous(result, outputs);
			}
		});

		finalResult.removeAll(toRemove);
	}

	private void setTaskOutputErroneous(SimpleCohortSizeResult result, List<OutputWrapper> outputs)
	{
		logger.error(
				"Final feasibility query result check failed for group with id '{}', not enough participating medics, expected >= {}, got {}",
				result.getCohortId(), MIN_PARTICIPATING_MEDICS, result.getParticipatingMedics());

		OutputWrapper outputWrapper = new OutputWrapper(Constants.CODESYSTEM_HIGHMED_BPMN);
		outputWrapper.addKeyValue(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, "Final feasibility query result check failed for group with id '" + result.getCohortId() + "', not enough participating medics, expected >= " + MIN_PARTICIPATING_MEDICS + ", got " + result.getParticipatingMedics());
		outputs.add(outputWrapper);
	}

	private void setTaskOutputsSuccessful(List<SimpleCohortSizeResult> finalResult, List<OutputWrapper> outputs)
	{
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
	}
}
