package org.highmed.dsf.bpe.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public abstract class AbstractTaskListener implements TaskListener
{
	@Override
	public void notify(DelegateTask task)
	{
		doNotify(task);
	}

	abstract protected void doNotify(DelegateTask task);
}
