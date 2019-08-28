package org.highmed.dsf.bpe.message;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.fhir.client.WebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UrlType;

import java.util.stream.Stream;

public class SendSimpleCohortSizeRequest extends AbstractTaskMessageSend
{
	public SendSimpleCohortSizeRequest(OrganizationProvider organizationProvider,
			WebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(organizationProvider, clientProvider, taskHelper);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);
		return Stream.of(toInputParameterResearchStudyReference(researchStudy.getId()),
				toInputParameterEndpointAddress(clientProvider.getLocalBaseUrl()));
	}

	private Task.ParameterComponent toInputParameterResearchStudyReference(String researchStudyId)
	{
		return new Task.ParameterComponent(new CodeableConcept(new Coding(Constants.CODESYSTEM_HIGHMED_BPMN,
				Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_RESEARCH_STUDY_REFERENCE, null)),
				new Reference().setReference(researchStudyId));
	}

	private Task.ParameterComponent toInputParameterEndpointAddress(String localBaseUrl)
	{
		return new Task.ParameterComponent(new CodeableConcept(
				new Coding(Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ENDPOINT_ADDRESS,
						null)), new UrlType().setValue(localBaseUrl));
	}
}
