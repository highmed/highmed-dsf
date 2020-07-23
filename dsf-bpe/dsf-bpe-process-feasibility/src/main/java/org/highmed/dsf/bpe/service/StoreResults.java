package org.highmed.dsf.bpe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResult;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.variables.FeasibilityQueryResultsValues;
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
				.getVariable(ConstantsFeasibility.VARIABLE_QUERY_RESULTS);

		boolean needsRecordLinkage = Boolean.TRUE
				.equals((Boolean) execution.getVariable(ConstantsFeasibility.VARIABLE_NEEDS_RECORD_LINKAGE));

		Task task = (Task) execution.getVariable(ConstantsBase.VARIABLE_TASK);

		List<FeasibilityQueryResult> extendedResults = new ArrayList<>();
		extendedResults.addAll(results.getResults());
		extendedResults.addAll(getResults(task, needsRecordLinkage));

		execution.setVariable(ConstantsFeasibility.VARIABLE_QUERY_RESULTS,
				FeasibilityQueryResultsValues.create(new FeasibilityQueryResults(extendedResults)));
	}

	private List<FeasibilityQueryResult> getResults(Task task, boolean needsRecordLinkage)
	{
		TaskHelper taskHelper = getTaskHelper();
		Reference requester = task.getRequester();

		if (needsRecordLinkage)
		{
			return taskHelper.getInputParameterWithExtension(task, ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE,
					ConstantsFeasibility.EXTENSION_GROUP_ID_URI).map(input -> {
				String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
				String resultSetUrl = ((Reference) input.getValue()).getReference();

				return FeasibilityQueryResult.idResult(requester.getIdentifier().getValue(), cohortId, resultSetUrl);
			}).collect(Collectors.toList());
		}
		else
		{
			return taskHelper.getInputParameterWithExtension(task, ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT,
					ConstantsFeasibility.EXTENSION_GROUP_ID_URI).map(input -> {
				String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
				int cohortSize = ((UnsignedIntType) input.getValue()).getValue();

				return FeasibilityQueryResult.countResult(requester.getIdentifier().getValue(), cohortId, cohortSize);
			}).collect(Collectors.toList());
		}
	}

}
