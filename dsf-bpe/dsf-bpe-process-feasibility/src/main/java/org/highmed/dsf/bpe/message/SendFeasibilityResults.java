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
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

@SuppressWarnings("unchecked")
public class SendFeasibilityResults extends AbstractTaskMessageSend
{
	public SendFeasibilityResults(OrganizationProvider organizationProvider,
			FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(organizationProvider, clientProvider, taskHelper);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		List<OutputWrapper> outputs = (List<OutputWrapper>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
		List<Task.ParameterComponent> inputs = new ArrayList<>();

		outputs.forEach(outputWrapper -> {
			outputWrapper.getKeyValueMap()
					.forEach(entry -> inputs.add(generateInputComponent(outputWrapper.getSystem(), entry.getKey(), entry.getValue())));
		});

		return inputs.stream();
	}

	private Task.ParameterComponent generateInputComponent(String system, String key, String value)
	{
		return new Task.ParameterComponent(new CodeableConcept(new Coding(system, key, null)), new StringType(value));
	}
}
