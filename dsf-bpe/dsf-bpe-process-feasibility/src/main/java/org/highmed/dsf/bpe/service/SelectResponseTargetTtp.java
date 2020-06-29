package org.highmed.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class SelectResponseTargetTtp extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public SelectResponseTargetTtp(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		String ttpIdentifier = (String) execution.getVariable(Constants.VARIABLE_TTP_IDENTIFIER);
		String correlationKey = getCorrelationKey(execution);

		MultiInstanceTarget ttpTarget = new MultiInstanceTarget(ttpIdentifier, correlationKey);
		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGET, MultiInstanceTargetValues.create(ttpTarget));
	}

	private String getCorrelationKey(DelegateExecution execution)
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		return getTaskHelper()
				.getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
						Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY)
				.orElseThrow(() -> new IllegalStateException(
						"No correlation key found, this error should have been caught by resource validation"));
	}
}
