package org.highmed.dsf.bpe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.bpe.variables.MultiInstanceResults;
import org.highmed.dsf.bpe.variables.FinalSimpleFeasibilityResult;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CalculateMultiMedicFeasibilityResults extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CalculateMultiMedicFeasibilityResults.class);

	public CalculateMultiMedicFeasibilityResults(FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		MultiInstanceResults resultsWrapper = (MultiInstanceResults) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS);
		List<Group> cohortDefinitions = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

		List<String> cohortIds = getCohortIds(cohortDefinitions);
		List<MultiInstanceResult> locationBasedResults = resultsWrapper.getResults();
		List<FinalSimpleFeasibilityResult> finalResult = calculateResults(cohortIds, locationBasedResults);

		// TODO: add percentage filter over result

		execution.setVariable(Constants.VARIABLE_SIMPLE_COHORT_SIZE_QUERY_FINAL_RESULT, finalResult);
	}

	private List<FinalSimpleFeasibilityResult> calculateResults(List<String> ids, List<MultiInstanceResult> results)
	{
		List<Map.Entry<String, String>> combinedResults = results.stream()
				.flatMap(result -> result.getQueryResults().entrySet().stream()).collect(Collectors.toList());

		List<FinalSimpleFeasibilityResult> resultsByCohortId = new ArrayList<>();

		ids.forEach(id -> {
			long participatingMedics = combinedResults.stream().filter(resultEntry -> resultEntry.getKey().equals(id))
					.count();
			long result = combinedResults.stream().filter(resultEntry -> resultEntry.getKey().equals(id))
					.mapToInt(resultEntry -> Integer.parseInt(resultEntry.getValue())).sum();
			resultsByCohortId.add(new FinalSimpleFeasibilityResult(id, participatingMedics, result));
		});

		return resultsByCohortId;
	}

	private List<String> getCohortIds(List<Group> cohortDefinitions)
	{
		return cohortDefinitions.stream().map(cohort -> {
			IdType cohortId = new IdType(cohort.getId());
			return cohortId.getResourceType() + "/" + cohortId.getIdPart();
		}).collect(Collectors.toList());
	}
}
