package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.springframework.beans.factory.InitializingBean;

public class SelectPingTargets extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public SelectPingTargets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
		List<Target> targets = organizationProvider.getRemoteIdentifiers().stream().map(identifier -> Target
				.createBiDirectionalTarget(identifier.getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(new Targets(targets)));
	}
}
