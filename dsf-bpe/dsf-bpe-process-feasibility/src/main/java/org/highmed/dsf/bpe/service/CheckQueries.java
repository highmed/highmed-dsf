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
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.dsf.fhir.variables.OutputsValues;
import org.hl7.fhir.r4.model.Group;
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
		Outputs outputs = (Outputs) execution.getVariable(ConstantsBase.VARIABLE_PROCESS_OUTPUTS);
		List<Group> cohorts = ((FhirResourcesList) execution.getVariable(ConstantsFeasibility.VARIABLE_COHORTS))
				.getResourcesAndCast();

		Map<String, String> queries = new HashMap<>();

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
				outputs.addErrorOutput(errorMessage);
			}
			else
			{
				queries.put(groupId, aqlQuery);
			}
		});

		execution.setVariable(ConstantsFeasibility.VARIABLE_QUERIES, queries);
		execution.setVariable(ConstantsBase.VARIABLE_PROCESS_OUTPUTS, OutputsValues.create(outputs));
	}
}
