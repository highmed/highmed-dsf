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
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
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
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Identifier ttpIdentifier = getTtpIdentifier(execution);
		String correlationKey = getCorrelationKey(execution);

		MultiInstanceTarget ttpTarget = new MultiInstanceTarget(ttpIdentifier.getValue(), correlationKey);
		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGET, MultiInstanceTargetValues.create(ttpTarget));
	}

	private Identifier getTtpIdentifier(DelegateExecution execution)
	{
		Organization ttp = (Organization) execution.getVariable(Constants.VARIABLE_TTP);

		return ttp.getIdentifier().stream()
				.filter(identifier -> identifier.getSystem().equals(organizationProvider.getDefaultIdentifierSystem()))
				.findFirst().get();
	}

	private String getCorrelationKey(DelegateExecution execution)
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		return getTaskHelper().getFirstInputParameterStringValue(task, Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElseThrow(() -> new IllegalStateException(
				"No correlation key found, this error should have been caught by resource validation"));
	}

}
