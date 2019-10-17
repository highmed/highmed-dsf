package org.highmed.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;

public class SendFeasibilityRequest extends AbstractTaskMessageSend
{
	private final FhirWebserviceClientProvider clientProvider;

	public SendFeasibilityRequest(OrganizationProvider organizationProvider,
			FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(organizationProvider, clientProvider, taskHelper);
		this.clientProvider = clientProvider;
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);
		IdType type = new IdType(clientProvider.getLocalBaseUrl() + "/" + researchStudy.getId());

		Task.ParameterComponent input = getTaskHelper().createInput(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE,
				new Reference().setReference(type.getValueAsString()));

		return Stream.of(input);
	}
}
