package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_COHORT_DEFINITIONS;
import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesList;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

public class CheckFeasibilityResources extends AbstractServiceDelegate
{
	public CheckFeasibilityResources(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);

		List<Group> cohorts = ((FhirResourcesList) execution.getVariable(Constants.VARIABLE_COHORTS))
				.getResourcesAndCast();

		checkNumberOfParticipatingMedics(researchStudy);
		checkNumberOfCohortDefinitions(cohorts);
	}

	private void checkNumberOfParticipatingMedics(ResearchStudy researchStudy)
	{
		long medics = researchStudy.getExtension().stream()
				.filter(e -> e.getUrl().equals(Constants.EXTENSION_PARTICIPATING_MEDIC_URI))
				.map(extension -> ((Reference) extension.getValue()).getReference()).distinct().count();

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
