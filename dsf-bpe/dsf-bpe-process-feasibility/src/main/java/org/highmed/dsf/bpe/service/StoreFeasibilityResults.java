package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.bpe.variables.MultiInstanceResults;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@SuppressWarnings("unchecked")
public class StoreFeasibilityResults extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StoreFeasibilityResults.class);

	private final OrganizationProvider organizationProvider;

	public StoreFeasibilityResults(OrganizationProvider organizationProvider, FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
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

		task.getInput().stream()
				.filter(input -> input.getType().getCoding().get(0).getSystem()
						.equals(Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY))
				.forEach(input -> queryResults.put(input.getType().getCoding().get(0).getCode().substring(Constants.NAMINGSYSTEM_HIGHMED_FEASIBILITY_VALUE_PREFIX_SINGLE_RESULT
						.length()), ((StringType) input.getValue()).getValue()));

		Identifier requesterIdentifier = organizationProvider.getIdentifier(new IdType(task.getRequester().getReference())).get();
		String requesterIdentifierString = requesterIdentifier.getSystem() + "|" + requesterIdentifier.getValue();

		MultiInstanceResult result = new MultiInstanceResult(requesterIdentifierString, queryResults);
		MultiInstanceResults resultsWrapper = (MultiInstanceResults) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS);
		resultsWrapper.add(result);

		// race conditions are not possible, since tasks are received sequentially over the websocket connection
		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULTS, resultsWrapper);
	}
}
