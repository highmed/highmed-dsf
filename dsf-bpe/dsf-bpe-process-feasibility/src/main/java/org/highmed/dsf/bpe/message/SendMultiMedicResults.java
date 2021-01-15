package org.highmed.dsf.bpe.message;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR;

import java.util.List;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsFeasibility;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.AbstractTaskMessageSend;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
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
				.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS);

		Stream<ParameterComponent> resultInputs = results.getResults().stream().flatMap(this::toInputs);
		Stream<ParameterComponent> errorInput = getErrorInput(execution);

		return Stream.concat(resultInputs, errorInput);
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
		return new Extension(ConstantsFeasibility.EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}

	private Stream<ParameterComponent> getErrorInput(DelegateExecution execution)
	{
		List<Task.TaskOutputComponent> outputs = getLeadingTaskFromExecutionVariables().getOutput();

		if (hasErrorOutput(outputs))
		{
			Task task = getLeadingTaskFromExecutionVariables();

			String taskUrl = new Reference(new IdType(getFhirWebserviceClientProvider().getLocalBaseUrl() + "/Task",
					task.getIdElement().getIdPart())).getReference();

			Task.ParameterComponent input = getTaskHelper()
					.createInput(CODESYSTEM_HIGHMED_BPMN, CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR,
							"Errors occurred for missing cohorts while calculating their multi medic feasibility "
									+ "result, see task with url='" + taskUrl + "'");
			return Stream.of(input);
		}

		return Stream.empty();
	}

	private boolean hasErrorOutput(List<Task.TaskOutputComponent> outputs)
	{
		return outputs.stream().anyMatch(output -> output.getType().getCoding().stream().anyMatch(
				coding -> coding.getSystem().equals(CODESYSTEM_HIGHMED_BPMN) && coding.getCode()
						.equals(CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR)));
	}
}