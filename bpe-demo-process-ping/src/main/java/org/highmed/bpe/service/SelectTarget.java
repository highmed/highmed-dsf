package org.highmed.bpe.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.bpe.Constants;
import org.highmed.fhir.organization.OrganizationProvider;
import org.highmed.fhir.variables.MultiInstanceTarget;
import org.highmed.fhir.variables.MultiInstanceTargetValues;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectTarget implements JavaDelegate, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectTargets.class);

	private final OrganizationProvider organizationProvider;

	public SelectTarget(OrganizationProvider organizationProvider)
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

		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		String correlationKey = getString(task.getInput(), Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).get();
		String targetOrganizationId = task.getRequester().getReference();

		execution.setVariable(Constants.VARIABLE_MULTIINSTANCE_TARGET,
				MultiInstanceTargetValues.create(new MultiInstanceTarget(targetOrganizationId, correlationKey)));
	}

	private Optional<String> getString(List<ParameterComponent> list, String system, String code)
	{
		return list.stream().filter(c -> c.getValue() instanceof StringType)
				.filter(c -> c.getType().getCoding().stream()
						.anyMatch(co -> system.equals(co.getSystem()) && code.equals(co.getCode())))
				.map(c -> ((StringType) c.getValue()).asStringValue()).findFirst();
	}
}
