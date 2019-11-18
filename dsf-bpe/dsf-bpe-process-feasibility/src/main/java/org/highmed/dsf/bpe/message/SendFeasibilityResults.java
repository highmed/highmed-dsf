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
import org.highmed.dsf.fhir.variables.Outputs;
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
		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		List<Task.ParameterComponent> inputs = new ArrayList<>();

		outputs.getOutputs().forEach(output -> {
			Task.ParameterComponent input = getTaskHelper()
					.createInput(output.getSystem(), output.getCode(), output.getValue());
			inputs.add(input);
		});

		return inputs.stream();
	}
}
