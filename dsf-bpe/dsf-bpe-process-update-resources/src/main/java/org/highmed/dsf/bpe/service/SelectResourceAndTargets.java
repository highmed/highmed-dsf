package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_BUNDLE_ID;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsUpdateResources.CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
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

	public SelectResourceAndTargets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
		Task task = getCurrentTaskFromExecutionVariables();
		List<Reference> references = getTaskHelper()
				.getInputParameterReferenceValues(task, CODESYSTEM_HIGHMED_UPDATE_RESOURCE,
						CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_BUNDLE_REFERENCE).collect(Collectors.toList());

		if (references.size() != 1)
		{
			logger.error("Task input contains unexpected number of Bundle IDs, expected 1, got {}", references.size());
			throw new RuntimeException(
					"Task input contains unexpected number of Bundle IDs, expected 1, got " + references.size());
		}
		else if (!BUNDLE_ID_PATTERN.matcher(references.get(0).getReference()).matches())
		{
			logger.error("Task input contains unexpected ids not matching {}", BUNDLE_ID_PATTERN_STRING);
			throw new RuntimeException("Task input contains unexpected ids not matching " + BUNDLE_ID_PATTERN_STRING);
		}

		String bundleId = references.get(0).getReference();
		execution.setVariable(BPMN_EXECUTION_VARIABLE_BUNDLE_ID, bundleId);

		List<String> targetIdentifierSearchParameters = getTaskHelper()
				.getInputParameterStringValues(task, CODESYSTEM_HIGHMED_UPDATE_RESOURCE,
						CODESYSTEM_HIGHMED_UPDATE_RESOURCE_VALUE_ORGANIZATION_IDENTIFIER_SEARCH_PARAMETER)
				.collect(Collectors.toList());

		List<Target> targets = targetIdentifierSearchParameters.stream()
				.flatMap(organizationProvider::searchRemoteOrganizationsIdentifiers)
				.map(identifier -> Target.createUniDirectionalTarget(identifier.getValue()))
				.collect(Collectors.toList());
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(new Targets(targets)));
	}
}
