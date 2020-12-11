package org.highmed.dsf.bpe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;
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
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.VARIABLE_QUERY_RESULTS);

		Task currentTask = getCurrentTaskFromExecutionVariables();
		List<FeasibilityQueryResult> filteredResults = filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(results, currentTask);

		// TODO: add percentage filter over results

		execution.setVariable(ConstantsFeasibility.VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(filteredResults)));
	}

	private List<FeasibilityQueryResult> filterErroneousResultsAndAddErrorsToCurrentTaskOutputs(
			FeasibilityQueryResults results, Task task)
	{
		List<FeasibilityQueryResult> filteredResults = new ArrayList<>();
		for (FeasibilityQueryResult result : results.getResults())
		{
			Optional<String> errorReason = testResultAndReturnErrorReason(result);
			if (errorReason.isPresent())
				addError(task, result.getCohortId(), errorReason.get());
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

	private void addError(Task task, String cohortId, String error)
	{
		String errorMessage = "Feasibility query result check failed for group with id '" + cohortId + "': " + error;
		logger.info(errorMessage);

		task.getOutput().add(getTaskHelper().createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, errorMessage));
	}
}
