package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FinalSimpleFeasibilityResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.dsf.fhir.variables.OutputsValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckMultiMedicFeasibilityResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckMultiMedicFeasibilityResults.class);

	public CheckMultiMedicFeasibilityResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void doExecute(DelegateExecution execution) throws Exception
	{
		List<FinalSimpleFeasibilityResult> finalResult = (List<FinalSimpleFeasibilityResult>) execution
				.getVariable(Constants.VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT);
		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		finalResult.stream().collect(Collectors.groupingBy(this::lowerThenThreshold)).forEach((isLower, list) -> {
			if (isLower)
				addErroneousResultsToOutputs(list.stream(), outputs);
			else
				addSuccessfulResultsToOutputs(list.stream(), outputs);
		});

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, OutputsValues.create(outputs));
	}

	private boolean lowerThenThreshold(FinalSimpleFeasibilityResult result)
	{
		return result.getParticipatingMedics() < MIN_PARTICIPATING_MEDICS;
	}

	private void addErroneousResultsToOutputs(Stream<FinalSimpleFeasibilityResult> erroneousResults, Outputs outputs)
	{
		erroneousResults.forEach(result -> {
			String errorMessage =
					"Final multi medic feasibility query result check failed for group with id '" + result.getCohortId()
							+ "', not enough participating medics, expected >= " + MIN_PARTICIPATING_MEDICS + ", got "
							+ result.getParticipatingMedics();

			logger.info(errorMessage);
			outputs.addErrorOutput(errorMessage);
		});
	}

	private void addSuccessfulResultsToOutputs(Stream<FinalSimpleFeasibilityResult> successfulResults, Outputs outputs)
	{
		successfulResults.forEach(result -> {
			outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
					Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS,
					result.getParticipatingMedics() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result
							.getCohortId());
			outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
					Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT,
					result.getCohortSize() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result
							.getCohortId());
		});
	}
}
