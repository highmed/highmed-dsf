package org.highmed.dsf.bpe.listener;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

public class DefaultBpmnParseListener extends AbstractBpmnParseListener
{

	private StartListener startListener;
	private EndListener endListener;

	public DefaultBpmnParseListener(StartListener startListener, EndListener endListener) {
		this.startListener = startListener;
		this.endListener = endListener;
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
}
