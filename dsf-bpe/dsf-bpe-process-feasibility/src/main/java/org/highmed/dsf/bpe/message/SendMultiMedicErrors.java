package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendMultiMedicErrors extends AbstractTaskMessageSend
{
	public SendMultiMedicErrors(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		Task task = getLeadingTaskFromExecutionVariables();

		String taskUrl = new Reference(new IdType(getFhirWebserviceClientProvider().getLocalBaseUrl() + "/Task" ,  task.getIdElement().getIdPart()))
				.getReference();

		Task.ParameterComponent input = getTaskHelper()
				.createInput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE,
						"An error occurred while calculating the multi medic feasibility result for "
								+ "all defined cohorts, see task with url='" + taskUrl + "'");
		return Stream.of(input);
	}
}
