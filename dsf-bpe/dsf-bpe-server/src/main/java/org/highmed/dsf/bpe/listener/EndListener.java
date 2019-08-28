package org.highmed.dsf.bpe.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

public class EndListener implements ExecutionListener
{

	private WebserviceClient webserviceClient;

	public EndListener(WebserviceClient webserviceClient)
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

			@SuppressWarnings("unchecked")
			Map<String, String> outputs = (Map<String, String>) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

			List<Task.TaskOutputComponent> outputComponents = new ArrayList<>();
			outputs.forEach((key, value) -> outputComponents.add(generateOutputComponent(key, value)));

			task.setOutput(outputComponents);
		}
		else
		{
			task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
		}

		task.setStatus(Task.TaskStatus.COMPLETED);
		webserviceClient.update(task);
	}

	private Task.TaskOutputComponent generateOutputComponent(String key, String value)
	{
		return new Task.TaskOutputComponent(
				new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_BPMN, key, null)), new StringType(value));
	}
}