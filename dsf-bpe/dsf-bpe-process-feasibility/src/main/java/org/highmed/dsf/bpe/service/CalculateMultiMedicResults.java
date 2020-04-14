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
import org.highmed.dsf.fhir.variables.FinalSimpleFeasibilityResult;
import org.highmed.dsf.fhir.variables.Outputs;

public class CalculateMultiMedicResults extends AbstractServiceDelegate
{
	public CalculateMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{

		List<FeasibilityQueryResult> locationBasedResults = ((FeasibilityQueryResults) execution
				.getVariable(Constants.VARIABLE_QUERY_RESULTS)).getResults();
		List<FinalSimpleFeasibilityResult> finalResults = calculateResults(locationBasedResults);

		// TODO: add percentage filter over result

		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		addResultsToOutput(outputs, finalResults);

		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private List<FinalSimpleFeasibilityResult> calculateResults(List<FeasibilityQueryResult> results)
	{
		return results.stream().map(FeasibilityQueryResult::getCohortId).distinct().map(groupId -> {
			long participatingMedics = results.stream().filter(resultEntry -> resultEntry.getCohortId().equals(groupId))
					.count();
			long result = results.stream().filter(resultEntry -> resultEntry.getCohortId().equals(groupId))
					.mapToInt(FeasibilityQueryResult::getCohortSize).sum();
			return new FinalSimpleFeasibilityResult(groupId, participatingMedics, result);
		}).collect(Collectors.toUnmodifiableList());
	}

	private void addResultsToOutput(Outputs outputs, List<FinalSimpleFeasibilityResult> finalResults)
	{
		finalResults.forEach(result -> {
			if (result.getParticipatingMedics() >= Constants.MIN_PARTICIPATING_MEDICS)
			{
				outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT,
						String.valueOf(result.getCohortSize()), Constants.EXTENSION_GROUP_ID_URI, result.getCohortId());
				;

				outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT,
						String.valueOf(result.getParticipatingMedics()), Constants.EXTENSION_GROUP_ID_URI,
						result.getCohortId());
			}
			else
			{
				outputs.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NOT_ENOUGH_PARTICIPATION,
						"Not enough participating MeDICs.", Constants.EXTENSION_GROUP_ID_URI, result.getCohortId());
			}
		});
	}
}
