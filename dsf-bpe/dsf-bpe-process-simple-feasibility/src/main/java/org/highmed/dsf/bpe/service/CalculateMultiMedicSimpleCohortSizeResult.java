package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.fhir.client.WebserviceClient;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CalculateMultiMedicSimpleCohortSizeResult extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CalculateMultiMedicSimpleCohortSizeResult.class);

	public CalculateMultiMedicSimpleCohortSizeResult(WebserviceClient webserviceClient) {
		super(webserviceClient);
	}

	@Override
	public void executeService(DelegateExecution execution) throws Exception
	{
		ArrayList<Integer> results = (ArrayList<Integer>) execution.getVariable(Constants.VARIABLE_COHORT_SIZE_RESULTS);

		// TODO: add percentage filter over result
		// TODO: change to multiinstance for multiple queries in research study
		int participatingMedics = results.size();
		int mutliMedicCohortSize = results.stream().reduce(0, Integer::sum);

		logger.info("Participating medics: {}", participatingMedics);
		logger.info("Multimedic cohort size: {}", mutliMedicCohortSize);

		execution.setVariable(Constants.VARIABLE_MULTI_MEDIC_PARTICIPATION_RESULT, participatingMedics);
		execution.setVariable(Constants.VARIABLE_MULTI_MEDIC_COHORT_SIZE_RESULT, mutliMedicCohortSize);
	}
}
