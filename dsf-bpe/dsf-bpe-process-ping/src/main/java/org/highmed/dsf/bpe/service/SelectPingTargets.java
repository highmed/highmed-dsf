package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectPingTargets extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPingTargets.class);

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
		List<MultiInstanceTarget> targets = organizationProvider.getRemoteIdentifiers().stream()
				.map(identifier -> new MultiInstanceTarget(identifier.getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		execution.setVariable("multiInstanceTargets",
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));
	}
}
