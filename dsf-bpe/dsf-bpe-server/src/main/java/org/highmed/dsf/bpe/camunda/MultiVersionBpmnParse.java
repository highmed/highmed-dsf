package org.highmed.dsf.bpe.camunda;

import java.util.List;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.ClassDelegateActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiVersionBpmnParse extends BpmnParse
{
	private static final Logger logger = LoggerFactory.getLogger(MultiVersionBpmnParse.class);

	private final DelegateProvider delegateProvider;

	public MultiVersionBpmnParse(BpmnParser parser, DelegateProvider delegateProvider)
	{
		super(parser);

		this.delegateProvider = delegateProvider;
	}

	@Override
	public void parseServiceTaskLike(ActivityImpl activity, String elementName, Element serviceTaskElement,
			Element camundaPropertiesElement, ScopeImpl scope)
	{
		super.parseServiceTaskLike(activity, elementName, serviceTaskElement, camundaPropertiesElement, scope);

		if (activity.getActivityBehavior() instanceof ClassDelegateActivityBehavior)
		{
			String className = serviceTaskElement.attributeNS(CAMUNDA_BPMN_EXTENSIONS_NS, PROPERTYNAME_CLASS);
			List<FieldDeclaration> fieldDeclarations = parseFieldDeclarations(serviceTaskElement);

			logger.debug("Modifying {} for {}", activity.getActivityBehavior().getClass().getSimpleName(), className);
			activity.setActivityBehavior(
					new MultiVersionClassDelegateActivityBehavior(className, fieldDeclarations, delegateProvider));
		}
		else
			logger.debug("Not modifying {}", activity.getActivityBehavior().getClass().getCanonicalName());
	}

	@Override
	protected TaskListener parseTaskListener(Element taskListenerElement, String taskElementId)
	{
		String className = taskListenerElement.attribute(PROPERTYNAME_CLASS);

		if (className != null)
		{
			List<FieldDeclaration> fieldDeclarations = parseFieldDeclarations(taskListenerElement);

			logger.debug("Using {} for {}", MultiVersionClassDelegateTaskListener.class.getName(), className);
			return new MultiVersionClassDelegateTaskListener(className, fieldDeclarations, delegateProvider);
		}
		else
		{
			TaskListener taskListener = super.parseTaskListener(taskListenerElement, taskElementId);

			logger.debug("Using default {} for {}", taskListener.getClass().getName(), className);
			return taskListener;
		}
	}
}
