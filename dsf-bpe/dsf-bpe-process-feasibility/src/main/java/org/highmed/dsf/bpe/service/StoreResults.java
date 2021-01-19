package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.springframework.beans.factory.InitializingBean;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public StoreResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		boolean needsRecordLinkage = Boolean.TRUE
				.equals((Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE));

		Task task = getCurrentTaskFromExecutionVariables();

		List<FeasibilityQueryResult> extendedResults = new ArrayList<>();
		extendedResults.addAll(results.getResults());
		extendedResults.addAll(getResults(task, needsRecordLinkage));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(extendedResults)));
	}

	private List<FeasibilityQueryResult> getResults(Task task, boolean needsRecordLinkage)
	{
		TaskHelper taskHelper = getTaskHelper();
		Reference requester = task.getRequester();

		if (needsRecordLinkage)
		{
			return taskHelper.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_FEASIBILITY,
					CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE, EXTENSION_HIGHMED_GROUP_ID)
					.map(input -> {
						String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
						String resultSetUrl = ((Reference) input.getValue()).getReference();

						return FeasibilityQueryResult
								.idResult(requester.getIdentifier().getValue(), cohortId, resultSetUrl);
					}).collect(Collectors.toList());
		}
		else
		{
			return taskHelper.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_FEASIBILITY,
					CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT, EXTENSION_HIGHMED_GROUP_ID).map(input -> {
				String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
				int cohortSize = ((UnsignedIntType) input.getValue()).getValue();

				return FeasibilityQueryResult.countResult(requester.getIdentifier().getValue(), cohortId, cohortSize);
			}).collect(Collectors.toList());
		}
	}

}
