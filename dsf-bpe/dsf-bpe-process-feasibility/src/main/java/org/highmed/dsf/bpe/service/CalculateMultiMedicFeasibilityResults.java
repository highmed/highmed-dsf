package org.highmed.dsf.bpe.service;

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
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;

public class CalculateMultiMedicFeasibilityResults extends AbstractServiceDelegate
{
	public CalculateMultiMedicFeasibilityResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void doExecute(DelegateExecution execution) throws Exception
	{
		MultiInstanceResults resultsWrapper = (MultiInstanceResults) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS);
		List<Group> cohortDefinitions = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

		Stream<String> cohortIds = getCohortIds(cohortDefinitions);
		List<MultiInstanceResult> locationBasedResults = resultsWrapper.getResults();
		List<FinalSimpleFeasibilityResult> finalResult = calculateResults(cohortIds, locationBasedResults);

		// TODO: add percentage filter over result

		execution.setVariable(Constants.VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT, finalResult);
	}

	private Stream<String> getCohortIds(List<Group> cohortDefinitions)
	{
		return cohortDefinitions.stream().map(cohort -> {
			IdType cohortId = new IdType(cohort.getId());
			return cohortId.getResourceType() + "/" + cohortId.getIdPart();
		});
	}

	private List<FinalSimpleFeasibilityResult> calculateResults(Stream<String> cohortIds, List<MultiInstanceResult> results)
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
