package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FinalSimpleFeasibilityResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.dsf.fhir.variables.Pair;
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
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		List<FinalSimpleFeasibilityResult> erroneousResults = checkNumberOfParticipatingMedics(finalResult);
		finalResult.removeAll(erroneousResults);

		OutputWrapper errorOutput = getOutputWrapperErroneous(erroneousResults);
		outputs.add(errorOutput);
		OutputWrapper successOutput = getOutputWrapperSuccessful(finalResult);
		outputs.add(successOutput);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	//  returns all results which are erroneous and did not have enough participating medics
	private List<FinalSimpleFeasibilityResult> checkNumberOfParticipatingMedics(
			List<FinalSimpleFeasibilityResult> finalResult)
	{
		return finalResult.stream().filter(result -> result.getParticipatingMedics() < MIN_PARTICIPATING_MEDICS)
				.collect(Collectors.toList());
	}

	private OutputWrapper getOutputWrapperErroneous(List<FinalSimpleFeasibilityResult> erroneousResults)
	{
		List<Pair<String, String>> errors = new ArrayList<>();
		erroneousResults.forEach(result -> {
			String errorMessage =
					"Final multi medic feasibility query result check failed for group with id '" + result.getCohortId()
							+ "', not enough participating medics, expected >= " + MIN_PARTICIPATING_MEDICS + ", got "
							+ result.getParticipatingMedics();

			logger.info(errorMessage);
			errors.add(new Pair<>(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage));
		});

		return new OutputWrapper(Constants.CODESYSTEM_HIGHMED_BPMN, errors);
	}

	private OutputWrapper getOutputWrapperSuccessful(List<FinalSimpleFeasibilityResult> successfulResults)
	{
		List<Pair<String, String>> success = new ArrayList<>();

		successfulResults.forEach(result -> {
			success.add(new Pair<>(Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS,
					result.getParticipatingMedics() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result
							.getCohortId()));
			success.add(new Pair<>(Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT,
					result.getCohortSize() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result
							.getCohortId()));
		});

		return new OutputWrapper(Constants.CODESYSTEM_HIGHMED_FEASIBILITY, success);
	}
}
