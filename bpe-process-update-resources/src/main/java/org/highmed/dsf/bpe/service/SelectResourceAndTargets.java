package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectResourceAndTargets implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectResourceAndTargets.class);

	private static final String PARAMETER_TARGET_IDENTIFIER = "target-identifier";
	private static final String PARAMETER_BUNDLE_ID = "bundle-id";
	private static final String BUNDLE_ID_PATTERN_STRING = "Bundle/.+";
	private static final Pattern BUNDLE_ID_PATTERN = Pattern.compile(BUNDLE_ID_PATTERN_STRING);

	private final OrganizationProvider organizationProvider;

	public SelectResourceAndTargets(OrganizationProvider organizationProvider)
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

		List<String> bundleIds = queryParameters.get(PARAMETER_BUNDLE_ID);
		logger.debug(PARAMETER_BUNDLE_ID + ": {}", bundleIds);

		if (bundleIds.size() != 1)
		{
			logger.error("Process parameter {} contains unexpected number of Bundle IDs, expected 1, got {}",
					PARAMETER_BUNDLE_ID, bundleIds.size());
			throw new RuntimeException("Process parameter " + PARAMETER_BUNDLE_ID
					+ " contains unexpected number of Bundle IDs, expected 1, got " + bundleIds.size());
		}
		else if (bundleIds.stream().anyMatch(id -> !BUNDLE_ID_PATTERN.matcher(id).matches()))
		{
			logger.error("Process parameter {} contains unexpected ids not matching {}", PARAMETER_BUNDLE_ID,
					BUNDLE_ID_PATTERN_STRING);
			throw new RuntimeException("Process parameter " + PARAMETER_BUNDLE_ID
					+ " contains unexpected ids not matching " + BUNDLE_ID_PATTERN_STRING);
		}

		List<String> targetIdentifierSearchParameters = queryParameters.get(PARAMETER_TARGET_IDENTIFIER);
		logger.debug(PARAMETER_TARGET_IDENTIFIER + ": {}", targetIdentifierSearchParameters);

		execution.setVariable(Constants.VARIABLE_BUNDLE_ID, bundleIds.get(0));

		List<MultiInstanceTarget> targets = targetIdentifierSearchParameters.stream()
				.flatMap(organizationProvider::searchRemoteOrganizationsIdentifiers)
				.map(identifier -> new MultiInstanceTarget(identifier.getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());
		execution.setVariable("multiInstanceTargets",
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));
	}
}
