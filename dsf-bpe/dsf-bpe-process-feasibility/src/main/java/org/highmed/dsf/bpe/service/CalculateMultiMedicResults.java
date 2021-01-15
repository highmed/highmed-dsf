package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsFeasibility;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;

public class CalculateMultiMedicResults extends AbstractServiceDelegate
{
	public CalculateMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		List<FeasibilityQueryResult> results = ((FeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS)).getResults();

		List<FinalFeasibilityQueryResult> finalResults = calculateResults(results);

		execution.setVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS,
				FinalFeasibilityQueryResultsValues.create(new FinalFeasibilityQueryResults(finalResults)));
	}

	private List<FinalFeasibilityQueryResult> calculateResults(List<FeasibilityQueryResult> results)
	{
		Map<String, List<FeasibilityQueryResult>> byCohortId = results.stream()
				.collect(Collectors.groupingBy(FeasibilityQueryResult::getCohortId));

		return byCohortId.entrySet().stream().map(e -> new FinalFeasibilityQueryResult(e.getKey(),
				toInt(e.getValue().stream().filter(r -> r.getCohortSize() > 0).count()),
				toInt(e.getValue().stream().mapToLong(FeasibilityQueryResult::getCohortSize).sum())))
				.collect(Collectors.toList());
	}

	private int toInt(long l)
	{
		if (l > Integer.MAX_VALUE)
			throw new IllegalArgumentException("long > " + Integer.MAX_VALUE);
		else
			return (int) l;
	}
}
