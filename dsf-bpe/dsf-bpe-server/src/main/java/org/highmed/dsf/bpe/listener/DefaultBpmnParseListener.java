package org.highmed.dsf.bpe.listener;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.beans.factory.InitializingBean;

/**
 * Adds before execution of a process a listener to every BPMN Start- and EndEvent as well as to CallActivities
 *
 * @see StartListener
 * @see CallActivityListener
 * @see EndListener
 */
public class DefaultBpmnParseListener extends AbstractBpmnParseListener implements InitializingBean
{
	private final StartListener startListener;
	private final EndListener endListener;
	private final CallActivityListener callActivityListener;

	public DefaultBpmnParseListener(StartListener startListener, EndListener endListener,
			CallActivityListener callActivityListener)
	{
		this.startListener = startListener;
		this.endListener = endListener;
		this.callActivityListener = callActivityListener;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(startListener, "startListener");
		Objects.requireNonNull(endListener, "endListener");
		Objects.requireNonNull(callActivityListener, "callActivityListener");
	}

	@Override
	public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity)
	{
		startEventActivity.addListener(ExecutionListener.EVENTNAME_START, startListener);
	}

	@Override
	public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl endEventActivity)
	{
		// Adding at index 0 to the end phase of the EndEvent, so processes can execute listeners after the Task
		// resource has been updated.
		// Listeners added to the end phase of the EndEvent via bpmn are execute after this listener
		endEventActivity.addListener(ExecutionListener.EVENTNAME_END, endListener, 0);
	}

	@Override
	public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity)
	{
		activity.addListener(ExecutionListener.EVENTNAME_START, callActivityListener);
	}
}
