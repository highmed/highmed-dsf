package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_COHORT_DEFINITIONS;
import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;

public class CheckFeasibilityResources extends AbstractServiceDelegate
{
	public CheckFeasibilityResources(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		// Do nothing if requester and recipient are the same, because check was already done in
		// the requestSimpleCohortSizeQuery process (leading medic).
		if (!task.getRequester().equalsDeep(task.getRestriction().getRecipient().get(0)))
		{
			ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);
			@SuppressWarnings("unchecked")
			List<Group> cohorts = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

			checkNumberOfParticipatingMedics(researchStudy);
			checkNumberOfCohortDefinitions(cohorts);
		}
	}

	private void checkNumberOfParticipatingMedics(ResearchStudy researchStudy)
	{
		long medics = researchStudy.getExtension().stream()
				.filter(e -> e.getUrl().equals(Constants.EXTENSION_PARTICIPATING_MEDIC_URI))
				.map(extension -> ((Reference) extension.getValue()).getReference())
				.distinct().count();

		if (medics < MIN_PARTICIPATING_MEDICS)
		{
			throw new IllegalStateException(
					"Number of distinct participanting MeDICs is < " + MIN_PARTICIPATING_MEDICS + ", got " + medics);
		}
	}

	private void checkNumberOfCohortDefinitions(List<Group> cohorts)
	{
		int size = cohorts.size();
		if (size < MIN_COHORT_DEFINITIONS)
		{
			throw new IllegalStateException(
					"Number of defined cohorts is < " + MIN_COHORT_DEFINITIONS + ", got " + cohorts.size());
		}
	}
}
