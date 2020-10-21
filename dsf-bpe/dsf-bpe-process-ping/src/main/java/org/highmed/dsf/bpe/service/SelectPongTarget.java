package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetValues;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SelectPongTarget extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPingTargets.class);

	public SelectPongTarget(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(ConstantsBase.VARIABLE_TASK);

		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(task,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN, ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).get();
		Identifier targetOrganizationIdentifier = task.getRequester().getIdentifier();

		execution.setVariable(ConstantsBase.VARIABLE_MULTI_INSTANCE_TARGET, MultiInstanceTargetValues
				.create(new MultiInstanceTarget(targetOrganizationIdentifier.getValue(), correlationKey)));
	}
}
