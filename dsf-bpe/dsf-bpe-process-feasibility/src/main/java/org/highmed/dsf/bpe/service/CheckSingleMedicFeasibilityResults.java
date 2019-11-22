package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.dsf.fhir.variables.OutputsValues;
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
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		MultiInstanceResult results = (MultiInstanceResult) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT);

		Map<String, String> finalResults = results.getQueryResults();

		// TODO: implement check and execute twice for filter in erroneous and correct results
		Map<String, String> erroneousResults = checkQueryResults(finalResults); // checkQueryResults(finalResults, negativeFilter);
		Map<String, String> correctResults = finalResults; // checkQueryResults(finalResults, positiveFilter);

		addErroneousResultsToOutputs(erroneousResults.entrySet().stream(), outputs);
		addSuccessfulResultsToOutputs(correctResults.entrySet().stream(), outputs);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, OutputsValues.create(outputs));
	}

	private Map<String, String> checkQueryResults(Map<String, String> queryResults)
	{
		Map<String, String> toRemove = new HashMap<>();

		queryResults.forEach((groupId, result) -> {
			// TODO implement check
		});

		return toRemove;
	}

	private void addErroneousResultsToOutputs(Stream<Map.Entry<String, String>> erroneousResults, Outputs outputs)
	{
		erroneousResults.forEach(entry -> {
			String errorMessage =
					"Final single medic feasibility query result check failed for group with id '" + entry.getKey()
							+ "', reason unknown";

			logger.info(errorMessage);
			outputs.addErrorOutput(errorMessage);
		});

	}

	private void addSuccessfulResultsToOutputs(Stream<Map.Entry<String, String>> successfulResults, Outputs outputs)
	{
		successfulResults.forEach(entry -> {
			outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
					Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT,
					entry.getValue() + CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + entry.getKey());
		});
	}
}
