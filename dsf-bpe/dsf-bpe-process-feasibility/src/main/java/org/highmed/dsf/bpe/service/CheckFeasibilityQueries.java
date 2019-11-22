package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.SIMPLE_FEASIBILITY_QUERY_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.dsf.fhir.variables.OutputsValues;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CheckFeasibilityQueries extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(CheckFeasibilityQueries.class);

	private final GroupHelper groupHelper;

	public CheckFeasibilityQueries(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			GroupHelper groupHelper)
	{
		super(clientProvider, taskHelper);
		this.groupHelper = groupHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(groupHelper, "groupHelper");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		List<Group> cohorts = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

		Map<String, String> queries = new HashMap<>();

		cohorts.forEach(group -> {
			String aqlQuery = groupHelper.extractAqlQuery(group).toLowerCase();

			IdType groupId = new IdType(group.getId());
			String groupIdString = groupId.getResourceType() + "/" + groupId.getIdPart();

			if (!aqlQuery.startsWith(SIMPLE_FEASIBILITY_QUERY_PREFIX))
			{
				String errorMessage =
						"Initial single medic feasibility query check failed, wrong format for query of group with id '"
								+ groupId + "', expected query to start with '" + SIMPLE_FEASIBILITY_QUERY_PREFIX
								+ "' but got '" + aqlQuery + "'";

				logger.info(errorMessage);
				outputs.addErrorOutput(errorMessage);
			}
			else
			{
				queries.put(groupIdString, aqlQuery);
			}
		});

		execution.setVariable(Constants.VARIABLE_QUERIES, queries);
		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, OutputsValues.create(outputs));
	}
}
