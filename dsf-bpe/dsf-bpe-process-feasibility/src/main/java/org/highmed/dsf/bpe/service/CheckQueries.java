package org.highmed.dsf.bpe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.group.GroupHelper;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesList;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CheckQueries extends AbstractServiceDelegate implements InitializingBean, JavaDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckQueries.class);

	private final GroupHelper groupHelper;

	public CheckQueries(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper, GroupHelper groupHelper)
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
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		List<Group> cohorts = ((FhirResourcesList) execution.getVariable(ConstantsFeasibility.VARIABLE_COHORTS))
				.getResourcesAndCast();

		Map<String, String> queries = new HashMap<>();

		Task leadingTask = getLeadingTaskFromExecutionVariables();
		cohorts.forEach(group -> {
			String aqlQuery = groupHelper.extractAqlQuery(group).toLowerCase();

			String groupId = group.getId();
			if (!aqlQuery.startsWith(ConstantsFeasibility.SIMPLE_FEASIBILITY_QUERY_PREFIX))
			{
				String errorMessage =
						"Initial single medic feasibility query check failed, wrong format for query of group with id '"
								+ groupId + "', expected query to start with '"
								+ ConstantsFeasibility.SIMPLE_FEASIBILITY_QUERY_PREFIX + "' but got '" + aqlQuery + "'";

				logger.info(errorMessage);
				leadingTask.getOutput().add(getTaskHelper().createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
						ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, errorMessage));
			}
			else
			{
				queries.put(groupId, aqlQuery);
			}
		});
		setLeadingTaskToExecutionVariables(leadingTask);

		execution.setVariable(ConstantsFeasibility.VARIABLE_QUERIES, queries);
	}
}
