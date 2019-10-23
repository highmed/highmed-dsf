package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.dsf.fhir.variables.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSingleMedicFeasibilityResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckSingleMedicFeasibilityResults.class);

	public CheckSingleMedicFeasibilityResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void doExecute(DelegateExecution execution) throws Exception
	{
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		MultiInstanceResult results = (MultiInstanceResult) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT);

		Map<String, String> finalResults = results.getQueryResults();
		Map<String, String> erroneousResults = checkQueryResults(finalResults);
		finalResults.keySet().removeAll(erroneousResults.keySet());

		OutputWrapper errorOutput = getOutputWrapperErroneous(erroneousResults);
		outputs.add(errorOutput);
		OutputWrapper successOutput = getOutputWrapperSuccessful(finalResults);
		outputs.add(successOutput);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private Map<String, String> checkQueryResults(Map<String, String> queryResults)
	{
		Map<String, String> toRemove = new HashMap<>();

		queryResults.forEach((groupId, result) -> {
			// TODO implement check
		});

		return toRemove;
	}

	private OutputWrapper getOutputWrapperErroneous(Map<String, String> erroneousResults)
	{
		List<Pair<String, String>> errors = new ArrayList<>();

		erroneousResults.forEach((groupId, result) -> {
			String errorMessage =
					"Final single medic feasibility query result check failed for group with id '" + groupId
							+ "', reason unknown";

			logger.info(errorMessage);
			errors.add(new Pair<>(Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage));
		});

		return new OutputWrapper(Constants.CODESYSTEM_HIGHMED_BPMN, errors);
	}

	private OutputWrapper getOutputWrapperSuccessful(Map<String, String> successfulResults)
	{
		List<Pair<String, String>> success = new ArrayList<>();
		successfulResults.forEach((groupId, result) -> success.add(new Pair<>(Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT,
					result + CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + groupId)));
		return new OutputWrapper(Constants.CODESYSTEM_HIGHMED_FEASIBILITY, success);
	}
}
