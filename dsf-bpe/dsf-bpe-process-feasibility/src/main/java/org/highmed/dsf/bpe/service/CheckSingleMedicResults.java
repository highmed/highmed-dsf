package org.highmed.dsf.bpe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.variables.Outputs;
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
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Outputs outputs = (Outputs) execution.getVariable(ConstantsBase.VARIABLE_PROCESS_OUTPUTS);

		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.VARIABLE_QUERY_RESULTS);

		List<FeasibilityQueryResult> filteredResults = filterErronesResultsAndAddErrorsToOutput(results, outputs);

		// TODO: add percentage filter over results

		execution.setVariable(ConstantsFeasibility.VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(filteredResults)));
	}

	private List<FeasibilityQueryResult> filterErronesResultsAndAddErrorsToOutput(FeasibilityQueryResults results,
			Outputs outputs)
	{
		List<FeasibilityQueryResult> filteredResults = new ArrayList<>();
		for (FeasibilityQueryResult result : results.getResults())
		{
			Optional<String> errorReason = testResultAndReturnErrorReason(result);
			if (errorReason.isPresent())
				addError(outputs, result.getCohortId(), errorReason.get());
			else
				filteredResults.add(result);
		}

		return filteredResults;
	}

	protected Optional<String> testResultAndReturnErrorReason(FeasibilityQueryResult result)
	{
		// TODO: implement check
		// cohort size > 0
		// other filter criteria tbd
		return Optional.empty();
	}

	private void addError(Outputs outputs, String cohortId, String error)
	{
		String errorMessage = "Feasibility query result check failed for group with id '" + cohortId + "': " + error;

		logger.info(errorMessage);
		outputs.addErrorOutput(errorMessage);
	}
}
