package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MultiInstanceResult;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.highmed.openehr.client.OpenehrWebserviceClient;
import org.highmed.openehr.model.datatypes.DV_Count;
import org.highmed.openehr.model.structur.ResultSet;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("unchecked")
public class ExecuteFeasibilityQueries extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(ExecuteFeasibilityQueries.class);

	private final OrganizationProvider organizationProvider;
	private final OpenehrWebserviceClient openehrWebserviceClient;

	public ExecuteFeasibilityQueries(OrganizationProvider organizationProvider, FhirWebserviceClient fhirWebserviceClient, OpenehrWebserviceClient openehrWebserviceClient,
			TaskHelper taskHelper)
	{
		super(fhirWebserviceClient, taskHelper);
		this.organizationProvider = organizationProvider;
		this.openehrWebserviceClient = openehrWebserviceClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(openehrWebserviceClient, "openehrWebserviceClient");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Map<String, String> queries = (Map<String, String>) execution.getVariable(Constants.VARIABLE_QUERIES);
		Map<String, String> results = new HashMap<>();

		queries.forEach((groupId, query) -> {
			executeQuery(groupId, query, results);
		});

		Identifier identifier = organizationProvider.getLocalIdentifier();
		MultiInstanceResult multiInstanceResult = new MultiInstanceResult(
				identifier.getSystem() + "|" + identifier.getValue(), results);

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_RESULT, multiInstanceResult);
	}

	private void executeQuery(String groupId, String query, Map<String, String> results)
	{
		logger.info("Executing aql-query '{}' for group '{}'", query, groupId);

		ResultSet result = openehrWebserviceClient.query(query, null);
		int count = ((DV_Count) result.getRow(0).get(0)).getValue();

//		Dummy Result:
//		int count = 10;

		results.put(groupId, String.valueOf(count));
	}

}
