package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.model.structure.ResultSet;
import org.springframework.beans.factory.InitializingBean;

public class ExecuteQueries extends AbstractServiceDelegate implements InitializingBean
{
	private final OpenEhrClient openehrClient;
	private final OrganizationProvider organizationProvider;

	public ExecuteQueries(FhirWebserviceClientProvider clientProvider, OpenEhrClient openehrClient,
			TaskHelper taskHelper, OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.openehrClient = openehrClient;
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(openehrClient, "openehrClient");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		// <groupId, query>
		@SuppressWarnings("unchecked")
		Map<String, String> queries = (Map<String, String>) execution.getVariable(ConstantsFeasibility.VARIABLE_QUERIES);

		Boolean needsConsentCheck = (Boolean) execution.getVariable(ConstantsFeasibility.VARIABLE_NEEDS_CONSENT_CHECK);
		Boolean needsRecordLinkage = (Boolean) execution.getVariable(ConstantsFeasibility.VARIABLE_NEEDS_RECORD_LINKAGE);
		boolean idQuery = Boolean.TRUE.equals(needsConsentCheck) || Boolean.TRUE.equals(needsRecordLinkage);

		List<FeasibilityQueryResult> results = queries.entrySet().stream()
				.map(entry -> executeQuery(entry.getKey(), entry.getValue(), idQuery)).collect(Collectors.toList());

		execution.setVariable(ConstantsFeasibility.VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(results)));
	}

	private FeasibilityQueryResult executeQuery(String cohortId, String cohortQuery, boolean idQuery)
	{
		// TODO We might want to introduce a more complex result type to represent a count,
		//      errors and possible meta-data.

		ResultSet resultSet = openehrClient.query(cohortQuery, null);

		if (idQuery)
		{
			return FeasibilityQueryResult.idResult(organizationProvider.getLocalIdentifierValue(), cohortId, resultSet);
		}
		else
		{
			int count = Integer.parseInt(resultSet.getRow(0).get(0).getValueAsString());
			return FeasibilityQueryResult.countResult(organizationProvider.getLocalIdentifierValue(), cohortId, count);
		}
	}
}
