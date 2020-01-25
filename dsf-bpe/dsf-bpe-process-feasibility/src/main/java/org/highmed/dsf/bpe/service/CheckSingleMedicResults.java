package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.dsf.fhir.variables.OutputsValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSingleMedicResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckSingleMedicResults.class);

	public CheckSingleMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		List<FeasibilityQueryResult> results = ((FeasibilityQueryResults) execution
				.getVariable(Constants.VARIABLE_QUERY_RESULTS)).getResults();

		Stream<FeasibilityQueryResult> positiveResults = checkQueryResults(results, getFilter());
		Stream<FeasibilityQueryResult> negativeResults = checkQueryResults(results, getNegativeFilter());

		addSuccessfulResultsToOutputs(positiveResults, outputs);
		addErroneousResultsToOutputs(negativeResults, outputs);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, OutputsValues.create(outputs));
	}

	private Stream<FeasibilityQueryResult> checkQueryResults(List<FeasibilityQueryResult> queryResults,
			Predicate<FeasibilityQueryResult> filter)
	{
		return queryResults.stream().filter(filter);
	}

	private Predicate<FeasibilityQueryResult> getFilter()
	{
		// TODO: implement check
		//		 cohort size > 0
		//       other filter criteria tbd

		return result -> true;
	}

	private Predicate<FeasibilityQueryResult> getNegativeFilter()
	{
		// TODO: implement check, should match the opposite criteria of getFilter()

		return result -> false;
	}

	private void addErroneousResultsToOutputs(Stream<FeasibilityQueryResult> erroneousResults, Outputs outputs)
	{
		erroneousResults.forEach(result -> {
			String errorMessage =
					"Final single medic feasibility query result check failed for group with id '" + result
							.getCohortId() + "', reason unknown";

			logger.info(errorMessage);
			outputs.addErrorOutput(errorMessage);
		});

	}

	private void addSuccessfulResultsToOutputs(Stream<FeasibilityQueryResult> successfulResults, Outputs outputs)
	{
		successfulResults.forEach(result -> {
			outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
					Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT,
					String.valueOf(result.getCohortSize()), Constants.EXTENSION_GROUP_ID_URI, result.getCohortId());
		});
	}
}
