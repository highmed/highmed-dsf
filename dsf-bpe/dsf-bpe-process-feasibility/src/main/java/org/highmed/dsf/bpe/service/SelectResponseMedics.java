package org.highmed.dsf.bpe.service;

import java.util.Objects;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class SelectResponseMedics extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public SelectResponseMedics(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(task,
				Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY)
				.orElse(UUID.randomUUID().toString());

		Identifier targetOrganizationIdentifier = organizationProvider
				.getIdentifier(new IdType(task.getRequester().getReference()))
				.orElseThrow(() -> new IllegalStateException(
						"Organization with id " + task.getRequester().getReference() + " not found"));

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGET, MultiInstanceTargetValues
				.create(new MultiInstanceTarget(targetOrganizationIdentifier.getValue(), correlationKey)));
	}
}
