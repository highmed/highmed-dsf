package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.client.OpenehrWebserviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ExecuteFeasibilityQueries extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ExecuteFeasibilityQueries.class);

	private final OpenehrWebserviceClient openehrWebserviceClient;

	public ExecuteFeasibilityQueries(FhirWebserviceClientProvider clientProvider,
			OpenehrWebserviceClient openehrWebserviceClient, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
		this.openehrWebserviceClient = openehrWebserviceClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(openehrWebserviceClient, "openehrWebserviceClient");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Map<String, String> queries = (Map<String, String>) execution.getVariable(Constants.VARIABLE_QUERIES);
		Map<String, String> results = new HashMap<>();

		queries.forEach((groupId, query) -> {
			String result = executeQuery(query);
			results.put(groupId, result);
		});

		MultiInstanceResult multiInstanceResult = new MultiInstanceResult(null, results);
		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT, multiInstanceResult);
	}

	private String executeQuery(String query)
	{
//		ResultSet result = openehrWebserviceClient.query(query, null);
//		int count = ((DV_Count) result.getRow(0).get(0)).getValue();

		// TODO: remove dummy result
		int count = 15;

		return String.valueOf(count);
	}

}
