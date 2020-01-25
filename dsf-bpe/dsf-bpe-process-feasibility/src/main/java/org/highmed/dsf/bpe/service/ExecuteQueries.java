package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.openehr.client.OpenehrWebserviceClient;
import org.springframework.beans.factory.InitializingBean;

public class ExecuteQueries extends AbstractServiceDelegate implements InitializingBean
{
	private final OpenehrWebserviceClient openehrWebserviceClient;

	public ExecuteQueries(FhirWebserviceClientProvider clientProvider, OpenehrWebserviceClient openehrWebserviceClient,
			TaskHelper taskHelper)
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
		// <groupId, query>
		Map<String, String> queries = (Map<String, String>) execution.getVariable(Constants.VARIABLE_QUERIES);

		List<FeasibilityQueryResult> results = queries.entrySet().stream().map(entry -> {
			int result = executeQuery(entry.getValue());
			return new FeasibilityQueryResult(null, entry.getKey(), result);
		}).collect(Collectors.toList());

		execution.setVariable(Constants.VARIABLE_QUERY_RESULTS, new FeasibilityQueryResults(results));
	}

	private int executeQuery(String query)
	{
		// TODO We might want to introduce a more complex result type to represent a count,
		//      errors and possible meta-data.

		//		ResultSet result = openehrWebserviceClient.query(query, null);
		//		int count = ((DvCount) result.getRow(0).get(0)).getValue();

		// TODO: remove dummy result
		return 15;
	}

}
