package org.highmed.dsf.bpe.message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;

public class SendFeasibilityResults extends AbstractTaskMessageSend
{
	public SendFeasibilityResults(OrganizationProvider organizationProvider,
			FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper, FhirContext fhirContext)
	{
		super(organizationProvider, clientProvider, taskHelper, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		@SuppressWarnings("unchecked")
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		List<Task.ParameterComponent> inputs = new ArrayList<>();

		outputs.forEach(outputWrapper -> outputWrapper.getKeyValueList().forEach(entry -> inputs
				.add(getTaskHelper().createInput(outputWrapper.getSystem(), entry.getKey(), entry.getValue()))));

		return inputs.stream();
	}
}
