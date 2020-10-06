package org.highmed.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class SelectResponseTargetMedic extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public SelectResponseTargetMedic(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
		Task task = (Task) execution.getVariable(ConstantsBase.VARIABLE_LEADING_TASK);

		String correlationKey = getTaskHelper()
				.getFirstInputParameterStringValue(task, ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
						ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElseThrow(
						() -> new IllegalStateException(
								"No correlation key found, this error should have been caught by resource validation"));

		MultiInstanceTarget medicTarget = new MultiInstanceTarget(task.getRequester().getIdentifier().getValue(),
				correlationKey);
		execution.setVariable(ConstantsBase.VARIABLE_MULTI_INSTANCE_TARGET,
				MultiInstanceTargetValues.create(medicTarget));
	}
}
