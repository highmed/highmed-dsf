package org.highmed.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsFeasibility;
import org.highmed.dsf.bpe.variables.BloomFilterConfig;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendMedicRequest extends AbstractTaskMessageSend
{
	public SendMedicRequest(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution
				.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);
		IdType researchStudyId = new IdType(
				getFhirWebserviceClientProvider().getLocalBaseUrl() + "/" + researchStudy.getId());

		ParameterComponent inputResearchStudyReference = getTaskHelper()
				.createInput(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE,
						new Reference().setReference(researchStudyId.toVersionless().getValueAsString()));

		boolean needsConsentCheck = (boolean) execution
				.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK);
		ParameterComponent inputNeedsConsentCheck = getTaskHelper()
				.createInput(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK,
						needsConsentCheck);

		boolean needsRecordLinkage = (boolean) execution
				.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);
		ParameterComponent inputNeedsRecordLinkage = getTaskHelper()
				.createInput(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE,
						needsRecordLinkage);

		if (needsRecordLinkage)
		{
			BloomFilterConfig bloomFilterConfig = (BloomFilterConfig) execution
					.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG);
			ParameterComponent inputBloomFilterConfig = getTaskHelper()
					.createInput(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
							ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG,
							bloomFilterConfig.toBytes());

			return Stream.of(inputResearchStudyReference, inputNeedsConsentCheck, inputNeedsRecordLinkage,
					inputBloomFilterConfig);
		}
		else
			return Stream.of(inputResearchStudyReference, inputNeedsConsentCheck, inputNeedsRecordLinkage);
	}
}
