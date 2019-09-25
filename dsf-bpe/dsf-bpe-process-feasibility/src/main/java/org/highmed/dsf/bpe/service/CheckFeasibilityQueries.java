package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.SIMPLE_FEASIBILITY_QUERY_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@SuppressWarnings("unchecked")
public class CheckFeasibilityQueries extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(CheckFeasibilityQueries.class);

	private GroupHelper groupHelper;

	public CheckFeasibilityQueries(FhirWebserviceClient webserviceClient, TaskHelper taskHelper,
			GroupHelper groupHelper)
	{
		super(webserviceClient, taskHelper);
		this.groupHelper = groupHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(groupHelper, "groupHelper");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		List<Group> cohorts = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);
		Map<String, String> queries = new HashMap<>();

		cohorts.forEach(group -> {
			String aqlQuery = groupHelper.extractQuery(group).toLowerCase();

			IdType groupId = new IdType(group.getId());
			String groupIdString = groupId.getResourceType() + "/" + groupId.getIdPart();

			if (!aqlQuery.startsWith(SIMPLE_FEASIBILITY_QUERY_PREFIX))
			{
				setTaskOutputErroneous(aqlQuery, groupIdString, outputs);
			}
			else
			{
				queries.put(groupIdString, aqlQuery);
			}
		});

		execution.setVariable(Constants.VARIABLE_QUERIES, queries);
		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, outputs);
	}

	private void setTaskOutputErroneous(String query, String groupId, List<OutputWrapper> outputs)
	{
		logger.error(
				"Initial feasibility query check failed, wrong format for query of group with id '{}', expected query to start with '{}' but got '{}'",
				groupId, SIMPLE_FEASIBILITY_QUERY_PREFIX, query);
		
		// TODO: add error to outputs for this erroneous query
	}
}
