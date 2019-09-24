package org.highmed.dsf.bpe.service;

import java.util.Objects;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectResponseMedics extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectResponseMedics.class);

	private final OrganizationProvider organizationProvider;
	private final TaskHelper taskHelper;

	public SelectResponseMedics(OrganizationProvider organizationProvider, TaskHelper taskHelper,
			FhirWebserviceClient webserviceClient)
	{
		super(webserviceClient, taskHelper);

		this.organizationProvider = organizationProvider;
		this.taskHelper = taskHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(taskHelper, "taskHelper");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		String correlationKey = taskHelper.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElse(UUID.randomUUID().toString());

		Identifier targetOrganizationIdentifier = organizationProvider
				.getIdentifier(new IdType(task.getRequester().getReference())).orElseThrow(
						() -> new IllegalStateException(
								"Organization with id " + task.getRequester().getReference() + " not found"));

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGET, MultiInstanceTargetValues
				.create(new MultiInstanceTarget(targetOrganizationIdentifier.getValue(), correlationKey)));
	}
}
