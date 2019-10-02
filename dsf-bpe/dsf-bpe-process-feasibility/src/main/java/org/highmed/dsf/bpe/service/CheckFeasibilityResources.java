package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_COHORT_DEFINITIONS;
import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CheckFeasibilityResources extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckFeasibilityResources.class);

	public CheckFeasibilityResources(FhirWebserviceClient webserviceClient, TaskHelper taskHelper)
	{
		super(webserviceClient, taskHelper);
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
			List<Group> cohorts = (List<Group>) execution.getVariable(Constants.VARIABLE_COHORTS);

			checkNumberOfParticipatingMedics(researchStudy);
			checkNumberOfCohortDefinitions(cohorts);
		}
	}

	private void checkNumberOfParticipatingMedics(ResearchStudy researchStudy)
	{
		List<Extension> medics = researchStudy.getExtension().stream()
				.filter(e -> e.getUrl().equals(Constants.EXTENSION_PARTICIPATING_MEDIC_URI))
				.collect(Collectors.toList());

		if (medics.size() < MIN_PARTICIPATING_MEDICS)
		{
			throw new IllegalArgumentException(
					"Number of participanting MeDICs is < " + MIN_PARTICIPATING_MEDICS + ", got " + medics.size());
		}
	}

	private void checkNumberOfCohortDefinitions(List<Group> cohorts)
	{
		int size = cohorts.size();
		if (size < MIN_COHORT_DEFINITIONS)
		{
			throw new IllegalArgumentException(
					"Number of defined cohorts is < " + MIN_COHORT_DEFINITIONS + ", got " + cohorts.size());
		}
	}
}
