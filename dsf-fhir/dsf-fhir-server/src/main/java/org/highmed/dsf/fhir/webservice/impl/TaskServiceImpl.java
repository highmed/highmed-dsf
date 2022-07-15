package org.highmed.dsf.fhir.webservice.impl;

import java.util.EnumSet;

import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.dao.TaskDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.history.HistoryService;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.TaskService;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskServiceImpl extends AbstractResourceServiceImpl<TaskDao, Task> implements TaskService
{
	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

	public TaskServiceImpl(String path, String serverBase, int defaultPageCount, TaskDao dao,
			ResourceValidator validator, EventHandler eventHandler, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, AuthorizationRuleProvider authorizationRuleProvider,
			HistoryService historyService)
	{
		super(path, Task.class, serverBase, defaultPageCount, dao, validator, eventHandler, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter, referenceExtractor, referenceResolver,
				referenceCleaner, authorizationRuleProvider, historyService);
	}

	// See also CheckReferencesCommand#checkReferenceAfterUpdate
	@Override
	protected boolean checkReferenceAfterUpdate(Task updated, ResourceReference ref)
	{
		if (EnumSet.of(TaskStatus.COMPLETED, TaskStatus.FAILED).contains(updated.getStatus()))
		{
			ReferenceType refType = ref.getType(serverBase);
			if ("Task.input".equals(ref.getLocation()) && ReferenceType.LITERAL_EXTERNAL.equals(refType))
			{
				logger.warn("Skipping check of {} reference '{}' at {} in resource with {}, version {}", refType,
						ref.getReference().getReference(), "Task.input", updated.getIdElement().getIdPart(),
						updated.getIdElement().getVersionIdPart());
				return false;
			}
		}

		return super.checkReferenceAfterUpdate(updated, ref);
	}
}
