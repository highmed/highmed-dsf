package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CheckResearchStudyCohortSize extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckResearchStudyCohortSize.class);

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	public static final int MIN_PARTICIPATING_MEDICS = 3;
	public static final int MIN_COHORT_DEFINITIONS = 1;

	public CheckResearchStudyCohortSize(WebserviceClient webserviceClient, TaskHelper taskHelper)
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

			doExecutePlugin(execution);
		}
	}

	private void checkNumberOfParticipatingMedics(ResearchStudy researchStudy)
	{
		List<Extension> medics = researchStudy.getExtension().stream()
				.filter(e -> e.getUrl().equals(Constants.EXTENSION_PARTICIPATING_MEDIC_URI))
				.collect(Collectors.toList());

		if (medics.size() < MIN_PARTICIPATING_MEDICS) {
			logger.error("Number of participanting MeDICs is <{}, got {}", MIN_PARTICIPATING_MEDICS, medics.size());
			throw new IllegalArgumentException("Number of participanting MeDICs is <" + MIN_PARTICIPATING_MEDICS + ", got " + medics.size());
		}
	}

	private void checkNumberOfCohortDefinitions(List<Group> cohorts)
	{
		if (cohorts.size() < MIN_COHORT_DEFINITIONS)
			logger.error("Number of defined cohorts is <{}, got {}", MIN_COHORT_DEFINITIONS, cohorts.size());
			throw new IllegalArgumentException("Number of defined cohorts is <" + MIN_COHORT_DEFINITIONS + ", got " + cohorts.size());
	}

	private void doExecutePlugin(DelegateExecution execution) {
		// TODO: distinguish between simple and complex query

		// TODO: implement plugin system for individual checks in different medics, like:
		// TODO:   - PI check
		// TODO:   - Queries check
		// TODO:   - Requester check
		// TODO:   - ...
	}
}
