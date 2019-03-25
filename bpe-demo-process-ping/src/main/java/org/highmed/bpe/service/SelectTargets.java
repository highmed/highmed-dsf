package org.highmed.bpe.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.variables.MultiInstanceTarget;
import org.highmed.fhir.variables.MultiInstanceTargets;
import org.highmed.fhir.variables.MultiInstanceTargetsValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectTargets implements JavaDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SelectTargets.class);

	private final OrganizationProvider organizationProvider;

	public SelectTargets(OrganizationProvider organizationProvider)
	{
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: Process-instance-id {}, business-key {}, variables {}, local-variables {}",
				getClass().getName(), execution.getProcessInstanceId(), execution.getBusinessKey(),
				execution.getVariables(), execution.getVariablesLocal());

		List<MultiInstanceTarget> targets = organizationProvider.getRemoteOrganizations().stream()
				.map(o -> new MultiInstanceTarget(o, UUID.randomUUID().toString())).collect(Collectors.toList());

		execution.setVariable("multiInstanceTargets",
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));
	}
}
