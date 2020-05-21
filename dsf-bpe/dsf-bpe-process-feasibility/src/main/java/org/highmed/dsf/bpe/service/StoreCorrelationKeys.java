package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.hl7.fhir.r4.model.Task;

public class StoreCorrelationKeys extends AbstractServiceDelegate
{
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

		execution.setVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGETS,
				MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);

		execution.setVariable(Constants.VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(null)));
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper().getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK).orElseThrow(
				() -> new IllegalArgumentException(
						"NeedsConsentCheck boolean is not set in task with id='" + task.getId()
								+ "', this error should " + "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper().getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE).orElseThrow(
				() -> new IllegalArgumentException(
						"NeedsRecordLinkage boolean is not set in task with id='" + task.getId()
								+ "', this error should " + "have been caught by resource validation"));
	}
}
