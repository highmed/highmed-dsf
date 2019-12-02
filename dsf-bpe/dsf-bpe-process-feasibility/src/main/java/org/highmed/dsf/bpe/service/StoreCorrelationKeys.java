package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreCorrelationKeys extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(StoreCorrelationKeys.class);

	public StoreCorrelationKeys(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		List<MultiInstanceTarget> targets = getTaskHelper()
				.getInputParameterStringValues(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY)
				.map(correlationKey -> new MultiInstanceTarget("", correlationKey)).collect(Collectors.toList());

		for (MultiInstanceTarget target : targets)
		{
			logger.info("Correlcation-Key: {}", target.getCorrelationKey());
		}

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGETS,
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));

		// TODO: replace by input variable from task input
		execution.setVariable(Constants.VARIABLE_NEEDS_RECORD_LINKAGE, false);
		execution.setVariable(Constants.VARIABLE_NEEDS_CONSENT_CHECK, false);
	}
}
