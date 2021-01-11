package org.highmed.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Targets;
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
		Targets multiInstanceTargets = (Targets) execution.getVariable(ConstantsBase.VARIABLE_TARGETS);

		Stream<Task.ParameterComponent> inputTargets = multiInstanceTargets.getEntries().stream()
				.map(target -> getTaskHelper().createInput(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY,
						target.getCorrelationKey()));

		boolean needsRecordLinkage = (boolean) execution
				.getVariable(ConstantsFeasibility.VARIABLE_NEEDS_RECORD_LINKAGE);
		Task.ParameterComponent inputNeedsRecordLinkage = getTaskHelper()
				.createInput(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE,
						needsRecordLinkage);

		return Stream.concat(inputTargets, Stream.of(inputNeedsRecordLinkage));
	}
}
