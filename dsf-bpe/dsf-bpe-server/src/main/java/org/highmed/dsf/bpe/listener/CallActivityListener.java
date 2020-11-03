package org.highmed.dsf.bpe.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.ConstantsBase;

/**
 * Added to each BPMN CallActivity Task by the {@link DefaultBpmnParseListener}.
 * Sets the variable {@link ConstantsBase#VARIABLE_IN_CALLED_PROCESS} to <code>true</code> if a  sub process was
 * called inside the same BPE.
 */
public class CallActivityListener implements ExecutionListener
{
	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		// mark that we enter a process called by another process.
		execution.setVariable(ConstantsBase.VARIABLE_IN_CALLED_PROCESS, true);
	}
}
