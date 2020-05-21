package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResultsValues;

public class GenerateCountFromIds extends AbstractServiceDelegate
{
	public GenerateCountFromIds(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(Constants.VARIABLE_QUERY_RESULTS);

		List<FeasibilityQueryResult> filteredResults = count(results.getResults());

		execution.setVariable(Constants.VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(filteredResults)));
	}

	private List<FeasibilityQueryResult> count(List<FeasibilityQueryResult> results)
	{
		return results.stream().map(this::count).collect(Collectors.toList());
	}

	protected FeasibilityQueryResult count(FeasibilityQueryResult result)
	{
		return FeasibilityQueryResult.countResult(result.getOrganizationIdentifier(), result.getCohortId(),
				result.getResultSet().getRows().size());
	}
}
