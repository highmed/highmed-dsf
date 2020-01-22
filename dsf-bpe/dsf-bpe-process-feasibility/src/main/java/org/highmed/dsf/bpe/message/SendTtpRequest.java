package org.highmed.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class SendTtpRequest extends AbstractTaskMessageSend
{
	public SendTtpRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		MultiInstanceTargets multiInstanceTargets = (MultiInstanceTargets) execution
				.getVariable(Constants.VARIABLE_MULTI_INSTANCE_TARGETS);

		Stream<Task.ParameterComponent> inputTargets = multiInstanceTargets.getTargets().stream()
				.map(target -> getTaskHelper().createInput(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY,
						target.getCorrelationKey()));

		boolean needsConsentCheck = (boolean) execution.getVariable(Constants.VARIABLE_NEEDS_CONSENT_CHECK);
		Task.ParameterComponent inputNeedsConsentCheck = getTaskHelper()
				.createInput(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = (boolean) execution.getVariable(Constants.VARIABLE_NEEDS_RECORD_LINKAGE);
		Task.ParameterComponent inputNeedsRecordLinkage = getTaskHelper()
				.createInput(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE	, needsRecordLinkage);

		return Stream.concat(inputTargets, Stream.of(inputNeedsConsentCheck, inputNeedsRecordLinkage));
	}
}
