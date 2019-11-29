package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FinalSimpleFeasibilityResult;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.bpe.variables.MultiInstanceResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Outputs;

public class CalculateMultiMedicResults extends AbstractServiceDelegate
{
	public CalculateMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void doExecute(DelegateExecution execution) throws Exception
	{
		MultiInstanceResults results = (MultiInstanceResults) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS);

		List<MultiInstanceResult> locationBasedResults = results.getResults();
		Stream<String> cohortIds = getCohortIds(locationBasedResults);
		List<FinalSimpleFeasibilityResult> finalResult = calculateResults(cohortIds, locationBasedResults);

		// TODO: add percentage filter over result

		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		finalResult.forEach(result -> {
			outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
					Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT,
					result.getCohortSize() + CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result.getCohortId());

			outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
					Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT,
					result.getParticipatingMedics() + CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR + result
							.getCohortId());
		});

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private Stream<String> getCohortIds(List<MultiInstanceResult> results)
	{
		return results.stream().flatMap(result -> result.getQueryResults().keySet().stream()).distinct();
	}

	private List<FinalSimpleFeasibilityResult> calculateResults(Stream<String> cohortIds,
			List<MultiInstanceResult> results)
	{
		List<Map.Entry<String, String>> combinedResults = results.stream()
				.flatMap(result -> result.getQueryResults().entrySet().stream()).collect(Collectors.toList());

		return cohortIds.map(id -> {
			long participatingMedics = combinedResults.stream().filter(resultEntry -> resultEntry.getKey().equals(id))
					.count();
			long result = combinedResults.stream().filter(resultEntry -> resultEntry.getKey().equals(id))
					.mapToInt(resultEntry -> Integer.parseInt(resultEntry.getValue())).sum();
			return new FinalSimpleFeasibilityResult(id, participatingMedics, result);
		}).collect(Collectors.toUnmodifiableList());
	}
}
