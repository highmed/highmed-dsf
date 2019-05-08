package org.highmed.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.bpe.Constants;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.variables.MultiInstanceTarget;
import org.highmed.fhir.variables.MultiInstanceTargets;
import org.highmed.fhir.variables.MultiInstanceTargetsValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectUpdateResourcesTargets implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectUpdateResourcesTargets.class);

	private static final String PARAMETER_TARGET_IDENTIFIER = "target-identifier";

	private final OrganizationProvider organizationProvider;

	public SelectUpdateResourcesTargets(OrganizationProvider organizationProvider)
	{
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: Process-instance-id {}, business-key {}, variables {}, local-variables {}",
				getClass().getName(), execution.getProcessInstanceId(), execution.getBusinessKey(),
				execution.getVariables(), execution.getVariablesLocal());

		@SuppressWarnings("unchecked")
		Map<String, List<String>> queryParameters = (Map<String, List<String>>) execution
				.getVariable(Constants.VARIABLE_QUERY_PARAMETERS);

		logger.debug(PARAMETER_TARGET_IDENTIFIER + ": {}", queryParameters.get(PARAMETER_TARGET_IDENTIFIER));

		List<String> targetIdentifierSearchParameters = queryParameters.get(PARAMETER_TARGET_IDENTIFIER);
		List<MultiInstanceTarget> targets = targetIdentifierSearchParameters.stream()
				.flatMap(organizationProvider::searchRemoteOrganizationsIdentifiers)
				.map(identifier -> new MultiInstanceTarget(identifier.getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		execution.setVariable("multiInstanceTargets",
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));
	}
}
