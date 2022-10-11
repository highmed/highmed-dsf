package org.highmed.dsf.bpe.camunda;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.ClassDelegateActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.highmed.dsf.bpe.delegate.DelegateProvider;
import org.highmed.dsf.bpe.listener.DefaultUserTaskListener;
import org.highmed.dsf.bpe.process.ProcessKeyAndVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiVersionBpmnParse extends BpmnParse
{
	private static final Logger logger = LoggerFactory.getLogger(MultiVersionBpmnParse.class);

	protected static final String TAGNAME_PROCESS = "process";
	protected static final String PROPERTYNAME_ID = "id";
	protected static final String PROPERTYNAME_VERSION = "http://camunda.org/schema/1.0/bpmn:versionTag";

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

			logger.debug("Modifying {} for {} in BPMN element with id '{}'",
					activity.getActivityBehavior().getClass().getSimpleName(), className,
					getElementId(serviceTaskElement));
			activity.setActivityBehavior(
					new MultiVersionClassDelegateActivityBehavior(className, fieldDeclarations, delegateProvider));
		}
		else
			logger.debug("Not modifying {} in BPMN element with id '{}'",
					activity.getActivityBehavior().getClass().getCanonicalName(), getElementId(serviceTaskElement));
	}

	@Override
	protected void parseTaskListeners(Element taskListenerElement, ActivityImpl timerActivity,
			TaskDefinition taskDefinition)
	{
		super.parseTaskListeners(taskListenerElement, timerActivity, taskDefinition);

		if (taskDefinition.getTaskListeners().getOrDefault(TaskListener.EVENTNAME_CREATE, new ArrayList<>()).stream()
				.filter(l -> l instanceof MultiVersionClassDelegateTaskListener)
				.map(l -> (MultiVersionClassDelegateTaskListener) l)
				.noneMatch(this::containsDefaultUserTaskListenerOrSuperClassOf))
		{
			logger.debug("Adding new {} for event '{}' to BPMN element with id '{}'",
					DefaultUserTaskListener.class.getName(), TaskListener.EVENTNAME_CREATE,
					getElementId(taskListenerElement));

			List<FieldDeclaration> fieldDeclarations = parseFieldDeclarations(taskListenerElement);
			TaskListener defaultUserTaskListener = new MultiVersionClassDelegateTaskListener(
					DefaultUserTaskListener.class.getName(), fieldDeclarations, delegateProvider);
			taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, defaultUserTaskListener);
		}
		else
		{
			logger.debug("Custom UserTaskListener extending {} is defined for event '{}' in BPMN element with id '{}'",
					DefaultUserTaskListener.class.getName(), TaskListener.EVENTNAME_CREATE,
					getElementId(taskListenerElement));
		}
	}

	private boolean containsDefaultUserTaskListenerOrSuperClassOf(
			MultiVersionClassDelegateTaskListener multiVersionClassDelegateTaskListener)
	{
		try
		{
			Element process = getRootElement().elements().stream().filter(e -> TAGNAME_PROCESS.equals(e.getTagName()))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("Root element does not contain process element"));

			ProcessKeyAndVersion processKeyAndVersion = new ProcessKeyAndVersion(getElementId(process),
					getElementVersion(process));

			Class<?> clazz = delegateProvider.getClassLoader(processKeyAndVersion)
					.loadClass(multiVersionClassDelegateTaskListener.getClassName());

			return DefaultUserTaskListener.class.isAssignableFrom(clazz);
		}
		catch (Exception exception)
		{
			throw new RuntimeException("Could not check if '" + DefaultUserTaskListener.class.getName()
					+ "' is assignable from '" + multiVersionClassDelegateTaskListener.getClassName() + "'", exception);
		}
	}

	@Override
	protected TaskListener parseTaskListener(Element taskListenerElement, String taskElementId)
	{
		String className = taskListenerElement.attribute(PROPERTYNAME_CLASS);

		if (className != null)
		{
			List<FieldDeclaration> fieldDeclarations = parseFieldDeclarations(taskListenerElement);

			logger.debug("Modifying {} for {} in BPMN element with id '{}'",
					MultiVersionClassDelegateTaskListener.class.getName(), className,
					getElementId(taskListenerElement));
			return new MultiVersionClassDelegateTaskListener(className, fieldDeclarations, delegateProvider);
		}
		else
		{
			TaskListener taskListener = super.parseTaskListener(taskListenerElement, taskElementId);
			logger.debug("Not modifying {} in BPMN element with id '{}", taskListener.getClass().getName(),
					getElementId(taskListenerElement));
			return taskListener;
		}
	}

	@Override
	public ExecutionListener parseExecutionListener(Element executionListenerElement, String ancestorElementId)
	{
		String className = executionListenerElement.attribute(PROPERTYNAME_CLASS);

		if (className != null)
		{
			List<FieldDeclaration> fieldDeclarations = parseFieldDeclarations(executionListenerElement);

			logger.debug("Modifying {} for {} in BPMN element with id '{}'",
					MultiVersionClassDelegateTaskListener.class.getName(), className,
					getElementId(executionListenerElement));
			return new MultiVersionClassDelegateExecutionListener(className, fieldDeclarations, delegateProvider);
		}
		else
		{
			ExecutionListener executionListener = super.parseExecutionListener(executionListenerElement,
					ancestorElementId);
			logger.debug("Not modifying {} in BPMN element with id '{}'", executionListener.getClass().getName(),
					getElementId(executionListenerElement));
			return executionListener;
		}
	}

	private String getElementId(Element element)
	{
		return element.attribute(PROPERTYNAME_ID);
	}

	private String getElementVersion(Element element)
	{
		return element.attribute(PROPERTYNAME_VERSION);
	}
}
