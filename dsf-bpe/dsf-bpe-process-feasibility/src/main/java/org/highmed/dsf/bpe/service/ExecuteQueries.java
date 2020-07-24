package org.highmed.dsf.bpe.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.client.OpenEhrWebserviceClient;
import org.highmed.openehr.model.datatypes.StringRowElement;
import org.highmed.openehr.model.structure.Column;
import org.highmed.openehr.model.structure.ResultSet;
import org.highmed.openehr.model.structure.RowElement;
import org.springframework.beans.factory.InitializingBean;

public class ExecuteQueries extends AbstractServiceDelegate implements InitializingBean
{
	private final OpenEhrWebserviceClient openehrWebserviceClient;
	private final OrganizationProvider organizationProvider;

	public ExecuteQueries(FhirWebserviceClientProvider clientProvider, OpenEhrWebserviceClient openehrWebserviceClient,
			TaskHelper taskHelper, OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.openehrWebserviceClient = openehrWebserviceClient;
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(openehrWebserviceClient, "openehrWebserviceClient");
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
		// errors and possible meta-data.

		// ResultSet result = openehrWebserviceClient.query(query, null);
		// int count = ((DvCount) result.getRow(0).get(0)).getValue();

		// TODO: remove dummy result

		if (idQuery)
		{
			List<List<RowElement>> rows = IntStream.range(0, 15)
					.mapToObj(id -> Collections.<RowElement> singletonList(new StringRowElement(String.valueOf(id))))
					.collect(Collectors.toList());
			ResultSet resultSet = new ResultSet(null, null, cohortQuery,
					Collections.singleton(new Column("EHRID", "/ehr_id/value")), rows);

			// returns ResultSet with EHRIDs 0, 1, ..., 14
			return FeasibilityQueryResult.idResult(organizationProvider.getLocalIdentifierValue(), cohortId, resultSet);
		}
		else
		{
			// returns 15
			return FeasibilityQueryResult.countResult(organizationProvider.getLocalIdentifierValue(), cohortId, 15);
		}
	}
}
