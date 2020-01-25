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

import ca.uhn.fhir.context.FhirContext;

public class SendMedicRequest extends AbstractTaskMessageSend
{
	public SendMedicRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);
		IdType type = new IdType(getFhirWebserviceClientProvider().getLocalBaseUrl() + "/" + researchStudy.getId());

		Task.ParameterComponent inputResearchStudyReference = getTaskHelper()
				.createInput(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE,
						new Reference().setReference(type.getValueAsString()));

		boolean needsConsentCheck = (boolean) execution.getVariable(Constants.VARIABLE_NEEDS_CONSENT_CHECK);
		Task.ParameterComponent inputNeedsConsentCheck = getTaskHelper()
				.createInput(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = (boolean) execution.getVariable(Constants.VARIABLE_NEEDS_RECORD_LINKAGE);
		Task.ParameterComponent inputNeedsRecordLinkage = getTaskHelper()
				.createInput(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);

		return Stream.of(inputResearchStudyReference, inputNeedsConsentCheck, inputNeedsRecordLinkage);
	}
}
