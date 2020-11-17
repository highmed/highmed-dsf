package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.process.BpmnFileAndModel;
import org.highmed.dsf.bpe.process.ProcessStateChangeOutcome;

public interface BpmnProcessStateChangeService
{
	/**
	 * @param models
	 *            models to deploy, not <code>null</code>
	 * @return list of state changes
	 */
	List<ProcessStateChangeOutcome> deploySuspendOrActivateProcesses(Stream<BpmnFileAndModel> models);
}
