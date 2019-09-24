package org.highmed.dsf.bpe.listener;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.variables.OutputWrapper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

@SuppressWarnings("unchecked")
public class EndListener implements ExecutionListener
{

	private FhirWebserviceClient webserviceClient;

	public EndListener(FhirWebserviceClient webserviceClient)
	{
		this.webserviceClient = webserviceClient;
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		Task task;
		if (execution.getParentId() == null)
		{
			// not in a subprocess --> end of main process
			task = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);

			List<OutputWrapper> outputs = (List<OutputWrapper>) execution
					.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);
			List<Task.TaskOutputComponent> outputComponents = new ArrayList<>();

			outputs.forEach(outputWrapper -> {
				outputWrapper.getKeyValueMap().forEach((key, value) -> outputComponents.add(generateOutputComponent(outputWrapper.getSystem(), key,value)));
			});

			task.setOutput(outputComponents);
		}
		else
		{
			task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
		}

		task.setStatus(Task.TaskStatus.COMPLETED);
		webserviceClient.update(task);
	}

	private Task.TaskOutputComponent generateOutputComponent(String system, String key, String value)
	{
		return new Task.TaskOutputComponent(new CodeableConcept(new Coding(system, key, null)), new StringType(value));
	}
}