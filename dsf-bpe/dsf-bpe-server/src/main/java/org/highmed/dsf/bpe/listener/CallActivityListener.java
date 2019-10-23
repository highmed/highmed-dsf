package org.highmed.dsf.bpe.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.highmed.dsf.bpe.Constants;

/**
 * Added to each CallActivity Task by the {@link DefaultBpmnParseListener}.
 * Can be used to execute certain things before the called process is executed.
 */
public class CallActivityListener implements ExecutionListener
{
	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		// mark that we enter a process called by another process.
		execution.setVariable(Constants.VARIABLE_IS_CALL_ACTIVITY, true);
	}
}
