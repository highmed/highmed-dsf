package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResultsValues;

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
				.getVariable(ConstantsBase.VARIABLE_QUERY_RESULTS)).getResults();

		// TODO: add percentage filter over result
		List<FinalFeasibilityQueryResult> finalResults = calculateResults(results);

		execution.setVariable(ConstantsBase.VARIABLE_FINAL_QUERY_RESULTS,
				FinalFeasibilityQueryResultsValues.create(new FinalFeasibilityQueryResults(finalResults)));
	}

	private List<FinalFeasibilityQueryResult> calculateResults(List<FeasibilityQueryResult> results)
	{
		Map<String, List<FeasibilityQueryResult>> byCohortId = results.stream()
				.collect(Collectors.groupingBy(FeasibilityQueryResult::getCohortId));

		return byCohortId.entrySet().stream()
				.map(e -> new FinalFeasibilityQueryResult(e.getKey(),
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
