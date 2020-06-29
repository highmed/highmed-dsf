package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FinalFeasibilityQueryResults;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.UnsignedIntType;

public class CheckMultiMedicResults extends AbstractServiceDelegate
{
	public CheckMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);
		// Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		FinalFeasibilityQueryResults results = readFinalFeasibilityQueryResults(task);
		results = checkResults(results);

		Task leadingTask = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);
		addOutputs(leadingTask, results);
	}

	private FinalFeasibilityQueryResults readFinalFeasibilityQueryResults(Task task)
	{
		List<FinalFeasibilityQueryResult> results = task.getInput().stream()
				.filter(in -> in.hasType() && in.getType().hasCoding()
						&& Constants.CODESYSTEM_HIGHMED_FEASIBILITY.equals(in.getType().getCodingFirstRep().getSystem())
						&& Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT
								.equals(in.getType().getCodingFirstRep().getCode()))
				.map(in -> toResult(task, in)).collect(Collectors.toList());
		return new FinalFeasibilityQueryResults(results);
	}

	private FinalFeasibilityQueryResult toResult(Task task, ParameterComponent in)
	{
		String cohortId = ((Reference) in.getExtensionByUrl(Constants.EXTENSION_GROUP_ID_URI).getValue())
				.getReference();
		int participatingMedics = getParticipatingMedicsCountByCohortId(task, cohortId);
		int cohortSize = ((UnsignedIntType) in.getValue()).getValue();
		return new FinalFeasibilityQueryResult(cohortId, participatingMedics, cohortSize);
	}

	private int getParticipatingMedicsCountByCohortId(Task task, String cohortId)
	{
		return task.getInput().stream().filter(in -> in.hasType() && in.getType().hasCoding()
				&& Constants.CODESYSTEM_HIGHMED_FEASIBILITY.equals(in.getType().getCodingFirstRep().getSystem())
				&& Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT
						.equals(in.getType().getCodingFirstRep().getCode())
				&& cohortId.equals(
						((Reference) in.getExtensionByUrl(Constants.EXTENSION_GROUP_ID_URI).getValue()).getReference()))
				.mapToInt(in -> ((UnsignedIntType) in.getValue()).getValue()).findFirst().getAsInt();
	}

	protected FinalFeasibilityQueryResults checkResults(FinalFeasibilityQueryResults results)
	{
		// TODO implement check for results
		// - criterias tbd
		return results;
	}

	private void addOutputs(Task leadingTask, FinalFeasibilityQueryResults results)
	{
		results.getResults().forEach(result -> addOutput(leadingTask, result));
	}

	private void addOutput(Task leadingTask, FinalFeasibilityQueryResult result)
	{
		TaskOutputComponent output1 = getTaskHelper().createOutputUnsignedInt(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT, result.getCohortSize());
		output1.addExtension(createCohortIdExtension(result.getCohortId()));
		leadingTask.addOutput(output1);

		TaskOutputComponent output2 = getTaskHelper().createOutputUnsignedInt(Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
				Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT,
				result.getParticipatingMedics());
		output2.addExtension(createCohortIdExtension(result.getCohortId()));
		leadingTask.addOutput(output2);
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(Constants.EXTENSION_GROUP_ID_URI, new Reference(cohortId));
	}
}
