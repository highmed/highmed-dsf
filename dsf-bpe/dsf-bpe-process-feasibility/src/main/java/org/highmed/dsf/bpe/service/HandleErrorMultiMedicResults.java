package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Task;

public class HandleErrorMultiMedicResults extends AbstractServiceDelegate
{
	public HandleErrorMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task currentTask = getCurrentTaskFromExecutionVariables();
		Task leadingTask = getLeadingTaskFromExecutionVariables();

		currentTask.getInput().forEach(input -> {
			boolean isErrorInput = input.getType().getCoding().stream().anyMatch(
					code -> code.getSystem().equals(ConstantsBase.CODESYSTEM_HIGHMED_BPMN) && code.getCode()
							.equals(ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR));

			if (isErrorInput)
			{
				leadingTask.getOutput().add(getTaskHelper().createOutput(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
						ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR, input.getValue().primitiveValue()));
			}
		});

		// The current task finishes here but is not automatically set to completed
		// because it is an additional task during the execution of the main process
		currentTask.setStatus(Task.TaskStatus.COMPLETED);
		getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(currentTask);
	}
}
