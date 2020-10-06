package org.highmed.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;

public class SendMultiMedicResults extends AbstractTaskMessageSend
{
	public SendMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		FinalFeasibilityQueryResults results = (FinalFeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.VARIABLE_FINAL_QUERY_RESULTS);

		return results.getResults().stream().flatMap(this::toInputs);
	}

	private Stream<ParameterComponent> toInputs(FinalFeasibilityQueryResult result)
	{
		ParameterComponent input1 = getTaskHelper()
				.createInputUnsignedInt(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT,
						result.getCohortSize());
		input1.addExtension(createCohortIdExtension(result.getCohortId()));

		ParameterComponent input2 = getTaskHelper()
				.createInputUnsignedInt(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT,
						result.getParticipatingMedics());
		input2.addExtension(createCohortIdExtension(result.getCohortId()));

		return Stream.of(input1, input2);
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(ConstantsFeasibility.EXTENSION_GROUP_ID_URI, new Reference(cohortId));
	}
}