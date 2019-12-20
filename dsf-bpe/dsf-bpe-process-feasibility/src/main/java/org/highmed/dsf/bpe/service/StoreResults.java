package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public StoreResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(Constants.VARIABLE_QUERY_RESULTS);

		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
		String requester = task.getRequester().getReference();

		List<FeasibilityQueryResult> resultInputs = getTaskHelper()
				.getInputParameterWithExtension(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT,
						Constants.EXTENSION_GROUP_ID_URI).map(input -> {
					String groupId = ((Reference) input.getExtension().get(0).getValue()).getReference();
					int groupSize = Integer.parseInt(input.getValue().primitiveValue());
					return new FeasibilityQueryResult(requester, groupId, groupSize);
				}).collect(Collectors.toList());

		results.addAll(resultInputs);

		// race conditions are not possible, since tasks are received sequentially over the websocket connection
		execution.setVariable(Constants.VARIABLE_QUERY_RESULTS, results);
	}
}
