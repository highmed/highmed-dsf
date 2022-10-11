package org.highmed.dsf.fhir.webservice.impl;

import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.dao.QuestionnaireDao;
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
import org.highmed.dsf.fhir.webservice.specification.QuestionnaireService;
import org.hl7.fhir.r4.model.Questionnaire;

public class QuestionnaireServiceImpl extends AbstractResourceServiceImpl<QuestionnaireDao, Questionnaire>
		implements QuestionnaireService
{
	public QuestionnaireServiceImpl(String path, String serverBase, int defaultPageCount,
			QuestionnaireDao questionnaireDao, ResourceValidator validator, EventHandler eventHandler,
			ExceptionHandler exceptionHandler, EventGenerator eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter, ReferenceExtractor referenceExtractor,
			ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			AuthorizationRuleProvider authorizationRuleProvider, HistoryService historyService)
	{
		super(path, Questionnaire.class, serverBase, defaultPageCount, questionnaireDao, validator, eventHandler,
				exceptionHandler, eventGenerator, responseGenerator, parameterConverter, referenceExtractor,
				referenceResolver, referenceCleaner, authorizationRuleProvider, historyService);
	}
}
