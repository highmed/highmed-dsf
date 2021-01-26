package org.highmed.dsf.fhir.webservice.impl;

import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.event.EventGenerator;
import org.highmed.dsf.fhir.event.EventHandler;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.history.HistoryService;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.ActivityDefinitionService;
import org.hl7.fhir.r4.model.ActivityDefinition;

public class ActivityDefinitionServiceImpl extends
		AbstractResourceServiceImpl<ActivityDefinitionDao, ActivityDefinition> implements ActivityDefinitionService
{
	public ActivityDefinitionServiceImpl(String path, String serverBase, int defaultPageCount,
			ActivityDefinitionDao dao, ResourceValidator validator, EventHandler eventHandler,
			ExceptionHandler exceptionHandler, EventGenerator eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter, ReferenceExtractor referenceExtractor,
			ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			AuthorizationRuleProvider authorizationRuleProvider, HistoryService historyService)
	{
		super(path, ActivityDefinition.class, serverBase, defaultPageCount, dao, validator, eventHandler,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter, referenceExtractor,
				referenceResolver, referenceCleaner, authorizationRuleProvider, historyService);
	}
}
