package org.highmed.dsf.bpe.listener;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.springframework.beans.factory.InitializingBean;

public class DefaultBpmnParseListener extends AbstractBpmnParseListener implements InitializingBean
{
	private final StartListener startListener;
	private final EndListener endListener;
	private final CallActivityListener callActivityListener;

	public DefaultBpmnParseListener(StartListener startListener, EndListener endListener, CallActivityListener callActivityListener) {
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
		super.parseStartEvent(startEventElement, scope, startEventActivity);
		startEventActivity.addListener(ExecutionListener.EVENTNAME_START, startListener);
	}

	@Override
	public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl endEventActivity)
	{
		super.parseEndEvent(endEventElement, scope, endEventActivity);
		endEventActivity.addListener(ExecutionListener.EVENTNAME_END, endListener);
	}

	@Override
	public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity)
	{
		super.parseCallActivity(callActivityElement, scope, activity);
		activity.addListener(ExecutionListener.EVENTNAME_START, callActivityListener);
	}
}
