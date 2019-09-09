package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectResourceAndTargets extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectResourceAndTargets.class);

	private static final String BUNDLE_ID_PATTERN_STRING = "Bundle/.+";
	private static final Pattern BUNDLE_ID_PATTERN = Pattern.compile(BUNDLE_ID_PATTERN_STRING);

	private final OrganizationProvider organizationProvider;
	private final TaskHelper taskHelper;

	public SelectResourceAndTargets(OrganizationProvider organizationProvider, WebserviceClient webserviceClient,
			TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
		this.organizationProvider = organizationProvider;
		this.taskHelper = taskHelper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: Process-instance-id {}, business-key {}, variables {}, local-variables {}",
				getClass().getName(), execution.getProcessInstanceId(), execution.getBusinessKey(),
				execution.getVariables(), execution.getVariablesLocal());

		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
		Supplier<Stream<Reference>> bundles = () -> taskHelper
				.getInputParameterReferenceValues(task, Constants.CODESYSTEM_HIGHMED_TASK_INPUT,
						Constants.CODESYSTEM_HIGHMED_TASK_INPUT_VALUE_BUNDLE_REFERENCE);

		if (bundles.get().count() != 1)
		{
			logger.error("Task input contains unexpected number of Bundle IDs, expected 1, got {}",
					bundles.get().count());
			throw new RuntimeException(
					"Task input contains unexpected number of Bundle IDs, expected 1, got " + bundles.get().count());
		}
		else if (bundles.get().anyMatch(reference -> !BUNDLE_ID_PATTERN.matcher(reference.getReference()).matches()))
		{
			logger.error("Task input contains unexpected ids not matching {}", BUNDLE_ID_PATTERN_STRING);
			throw new RuntimeException("Task input contains unexpected ids not matching " + BUNDLE_ID_PATTERN_STRING);
		}

		String bundleId = bundles.get().findFirst().get().getId();
		execution.setVariable(Constants.VARIABLE_BUNDLE_ID, bundleId);

		List<String> targetIdentifierSearchParameters = taskHelper
				.getInputParameterStringValues(task, Constants.CODESYSTEM_HIGHMED_TASK_INPUT,
						Constants.CODESYSTEM_HIGHMED_TASK_INPUT_VALUE_TARGET_IDENTIFIER).collect(Collectors.toList());

		List<MultiInstanceTarget> targets = targetIdentifierSearchParameters.stream()
				.flatMap(organizationProvider::searchRemoteOrganizationsIdentifiers)
				.map(identifier -> new MultiInstanceTarget(identifier.getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());
		execution.setVariable("multiInstanceTargets",
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));
	}
}
