package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.bpe.variables.MultiInstanceResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class StoreFeasibilityResults extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public StoreFeasibilityResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		Map<String, String> queryResults = new HashMap<>();
		Stream<String> resultInputs = getTaskHelper().getInputParameterStringValues(task,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT);

		resultInputs.forEach(input ->
		{
			String[] resultParts = input.split("\\" + Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_SEPARATOR);
			queryResults.put(resultParts[Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_GROUP_ID_INDEX],
					resultParts[Constants.CODESYSTEM_HIGHMED_FEASIBILITY_RESULT_COHORT_SIZE_INDEX]);
		});

		String requesterReference = task.getRequester().getReference();
		Identifier requesterIdentifier = organizationProvider.getIdentifier(new IdType(requesterReference))
				.orElseThrow(() -> new IllegalStateException(
						"Could not find organization reference: " + requesterReference + ", dropping received result"));

		MultiInstanceResult result = new MultiInstanceResult(
				requesterIdentifier.getSystem() + "|" + requesterIdentifier.getValue(), queryResults);

		MultiInstanceResults resultsWrapper = (MultiInstanceResults) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS);
		resultsWrapper.add(result);

		// race conditions are not possible, since tasks are received sequentially over the websocket connection
		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS, resultsWrapper);
	}
}
