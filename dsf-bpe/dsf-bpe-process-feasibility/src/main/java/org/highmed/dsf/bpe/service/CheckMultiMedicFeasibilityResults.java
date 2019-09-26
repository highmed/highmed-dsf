package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.SimpleCohortSizeResult;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
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

		List<SimpleCohortSizeResult> erroneousResults = checkNumberOfParticipatingMedics(finalResult);
		finalResult.removeAll(erroneousResults);

		// TODO: more checks

		OutputWrapper errorOutput = getOutputWrapperErroneous(erroneousResults);
		outputs.add(errorOutput);
		OutputWrapper successOutput = getOutputWrapperSuccessful(finalResult);
		outputs.add(successOutput);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private List<SimpleCohortSizeResult> checkNumberOfParticipatingMedics(List<SimpleCohortSizeResult> finalResult)
	{
		List<SimpleCohortSizeResult> toRemove = new ArrayList<>();

		finalResult.forEach(result -> {
			if (result.getParticipatingMedics() < MIN_PARTICIPATING_MEDICS)
			{
				toRemove.add(result);
			}
		});

		return toRemove;
	}

	private OutputWrapper getOutputWrapperErroneous(List<SimpleCohortSizeResult> erroneousResults)
	{
		OutputWrapper outputWrapper = new OutputWrapper(Constants.CODESYSTEM_HIGHMED_BPMN);
		erroneousResults.forEach(result -> {
			outputWrapper.addKeyValue(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE,
					"Final multi medic feasibility query result check failed for group with id '" + result.getCohortId()
							+ "', not enough participating medics, expected >= " + MIN_PARTICIPATING_MEDICS + ", got "
							+ result.getParticipatingMedics());
		});
		return outputWrapper;
	}

	private OutputWrapper getOutputWrapperSuccessful(List<SimpleCohortSizeResult> successfulResults)
	{
		OutputWrapper outputWrapper = new OutputWrapper(Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY);

		successfulResults.forEach(simpleCohortSizeResult -> {
			outputWrapper.addKeyValue(Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY_VALUE_PREFIX_PARTICIPATING_MEDICS
							+ simpleCohortSizeResult.getCohortId(),
					String.valueOf(simpleCohortSizeResult.getParticipatingMedics()));
			outputWrapper.addKeyValue(
					Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY_VALUE_PREFIX_COHORT_SIZE + simpleCohortSizeResult
							.getCohortId(), String.valueOf(simpleCohortSizeResult.getCohortSize()));
		});

		return outputWrapper;
	}
}
