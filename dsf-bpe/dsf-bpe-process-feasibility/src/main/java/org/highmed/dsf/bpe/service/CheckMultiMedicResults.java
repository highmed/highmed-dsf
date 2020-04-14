package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Outputs;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

public class CheckMultiMedicResults extends AbstractServiceDelegate
{
	public CheckMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		// Check for enough participating MeDICs and result filter application is done on the TTP

		// TODO implement check for results
		//      - criterias tbd

		transformAndAddToTaskOutput(outputs, task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT);
		transformAndAddToTaskOutput(outputs, task,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT);
		transformAndAddToTaskOutput(outputs, task,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NOT_ENOUGH_PARTICIPATION);
	}

	private void transformAndAddToTaskOutput(Outputs outputs, Task task, String code)
	{
		getTaskHelper().getInputParameterWithExtension(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY, code,
				Constants.EXTENSION_GROUP_ID_URI).forEach(result -> outputs
				.add(Constants.CODESYSTEM_HIGHMED_FEASIBILITY, code, result.getValue().primitiveValue(),
						Constants.EXTENSION_GROUP_ID_URI,
						((Reference) result.getExtension().get(0).getValue()).getReference()));
	}
}
