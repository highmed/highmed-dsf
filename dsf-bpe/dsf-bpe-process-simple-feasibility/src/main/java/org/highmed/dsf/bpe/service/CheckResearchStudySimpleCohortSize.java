package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class CheckResearchStudySimpleCohortSize extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckResearchStudySimpleCohortSize.class);

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	public static final int MIN_PARTICIPATING_MEDICS = 3;

	public CheckResearchStudySimpleCohortSize(WebserviceClient webserviceClient) {
		super(webserviceClient);
	}

	@Override
	public void executeService(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		// Do nothing if requester and recipient are the same, because check was already done in
		// the requestSimpleCohortSizeQuery process (this is the leading medic).
		if (!task.getRequester().equalsDeep(task.getRestriction().getRecipient().get(0)))
		{
			ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);

			checkNumberOfParticipatingMedics(researchStudy);

			// TODO: implement check for PI
			// TODO: implement check for Queries
			// TODO: implement check for ...
		}
	}

	private void checkNumberOfParticipatingMedics(ResearchStudy researchStudy)
	{
		List<Extension> medics = researchStudy.getExtension().stream()
				.filter(e -> e.getUrl().equals(Constants.EXTENSION_PARTICIPATING_MEDIC_URI))
				.collect(Collectors.toList());

		if (medics.size() < MIN_PARTICIPATING_MEDICS)
			stopInstance("Number of participating MeDICs <" + MIN_PARTICIPATING_MEDICS);
	}

	private void stopInstance(String reason)
	{
		logger.error("ResearchStudy review failed, reason {}", reason);
		throw new RuntimeException("ResearchStudy review failed, reason " + reason);
	}
}
