package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FinalSimpleFeasibilityResult;
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
		List<FinalSimpleFeasibilityResult> finalResult = (List<FinalSimpleFeasibilityResult>) execution
				.getVariable(Constants.VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT);
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		List<FinalSimpleFeasibilityResult> erroneousResults = checkNumberOfParticipatingMedics(finalResult);
		finalResult.removeAll(erroneousResults);

		OutputWrapper errorOutput = getOutputWrapperErroneous(erroneousResults);
		outputs.add(errorOutput);
		OutputWrapper successOutput = getOutputWrapperSuccessful(finalResult);
		outputs.add(successOutput);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private List<FinalSimpleFeasibilityResult> checkNumberOfParticipatingMedics(List<FinalSimpleFeasibilityResult> finalResult)
	{
		List<FinalSimpleFeasibilityResult> toRemove = new ArrayList<>();

		finalResult.forEach(result -> {
			if (result.getParticipatingMedics() < MIN_PARTICIPATING_MEDICS)
			{
				toRemove.add(result);
			}
		});

		return toRemove;
	}

	private OutputWrapper getOutputWrapperErroneous(List<FinalSimpleFeasibilityResult> erroneousResults)
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

	private OutputWrapper getOutputWrapperSuccessful(List<FinalSimpleFeasibilityResult> successfulResults)
	{
		OutputWrapper outputWrapper = new OutputWrapper(Constants.CODESYSTEM_HIGHMED_FEASIBILITY);
		successfulResults.forEach(result -> {
			outputWrapper.addKeyValue(Constants.CODSYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS,
					result.getParticipatingMedics() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result.getCohortId());
			outputWrapper.addKeyValue(
					Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT, result.getCohortSize() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR +  result
							.getCohortId());
		});

		return outputWrapper;
	}
}
