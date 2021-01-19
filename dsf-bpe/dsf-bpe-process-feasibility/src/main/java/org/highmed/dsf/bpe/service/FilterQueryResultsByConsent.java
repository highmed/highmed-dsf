package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsFeasibility;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.model.structure.ResultSet;

public class FilterQueryResultsByConsent extends AbstractServiceDelegate
{
	public FilterQueryResultsByConsent(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		List<FeasibilityQueryResult> filteredResults = filterResults(results.getResults());

		execution.setVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(filteredResults)));
	}

	private List<FeasibilityQueryResult> filterResults(List<FeasibilityQueryResult> results)
	{
		return results.stream().map(this::filterResult).collect(Collectors.toList());
	}

	protected FeasibilityQueryResult filterResult(FeasibilityQueryResult result)
	{
		return FeasibilityQueryResult.idResult(result.getOrganizationIdentifier(), result.getCohortId(),
				filterResultSet(result.getResultSet()));
	}

	private ResultSet filterResultSet(ResultSet resultSet)
	{
		// TODO implement

		return resultSet;
	}
}
