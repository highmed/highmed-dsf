package org.highmed.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.highmed.dsf.fhir.variables.MultiInstanceTarget;
import org.highmed.dsf.fhir.variables.MultiInstanceTargets;
import org.highmed.dsf.fhir.variables.MultiInstanceTargetsValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SelectTargetMedics implements JavaDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SelectTargetMedics.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception
	{
		logger.debug("{}: executing ...", getClass().getName());

		// TODO: implement

		// TODO: Add correct target medics from ResearchStudy
		List<MultiInstanceTarget> targets = Arrays.asList(new MultiInstanceTarget("test", "test"));
		execution.setVariable("multiInstanceTargets", MultiInstanceTargetsValues.create(new MultiInstanceTargets(targets)));
	}
}
