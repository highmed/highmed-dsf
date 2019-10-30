package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		List<FinalSimpleFeasibilityResult> erroneousResults = filterNumberOfParticipatingMedics(finalResult, lowerThenThreshold());
		List<FinalSimpleFeasibilityResult> correctResults = filterNumberOfParticipatingMedics(finalResult, higherOrEqualThenThreshold());

		outputs.add(getOutputWrapperErroneous(erroneousResults.stream()));
		outputs.add(getOutputWrapperSuccessful(correctResults.stream()));

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private List<FinalSimpleFeasibilityResult> filterNumberOfParticipatingMedics(
			List<FinalSimpleFeasibilityResult> finalResult, Predicate<FinalSimpleFeasibilityResult> thresholdFilter)
	{
		return finalResult.stream().filter(thresholdFilter).collect(Collectors.toList());
	}

	private Predicate<FinalSimpleFeasibilityResult> lowerThenThreshold()
	{
		return result -> result.getParticipatingMedics() < MIN_PARTICIPATING_MEDICS;
	}

	private Predicate<FinalSimpleFeasibilityResult> higherOrEqualThenThreshold()
	{
		return result -> result.getParticipatingMedics() >= MIN_PARTICIPATING_MEDICS;
	}

	private OutputWrapper getOutputWrapperErroneous(Stream<FinalSimpleFeasibilityResult> erroneousResults)
	{
		List<Pair<String, String>> errors = erroneousResults.map(result -> {
			String errorMessage =
					"Final multi medic feasibility query result check failed for group with id '" + result.getCohortId()
							+ "', not enough participating medics, expected >= " + MIN_PARTICIPATING_MEDICS + ", got "
							+ result.getParticipatingMedics();

			logger.info(errorMessage);
			return new Pair<>(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage);
		}).collect(Collectors.toList());

		return new OutputWrapper(Constants.CODESYSTEM_HIGHMED_BPMN, errors);
	}

	private OutputWrapper getOutputWrapperSuccessful(Stream<FinalSimpleFeasibilityResult> successfulResults)
	{
		List<Pair<String, String>> success = successfulResults.flatMap(result -> Stream.of(
			new Pair<>(Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS,
					result.getParticipatingMedics() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result
							.getCohortId()),
			new Pair<>(Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT,
					result.getCohortSize() + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result
							.getCohortId()))).collect(Collectors.toList());

		return new OutputWrapper(Constants.CODESYSTEM_HIGHMED_FEASIBILITY, success);
	}
}
