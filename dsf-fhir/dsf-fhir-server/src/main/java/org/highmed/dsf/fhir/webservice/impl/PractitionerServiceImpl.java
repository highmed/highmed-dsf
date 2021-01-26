package org.highmed.dsf.fhir.webservice.impl;

import org.highmed.dsf.fhir.authorization.AuthorizationRuleProvider;
import org.highmed.dsf.fhir.dao.PractitionerDao;
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
import org.highmed.dsf.fhir.webservice.specification.PractitionerService;
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerServiceImpl extends AbstractResourceServiceImpl<PractitionerDao, Practitioner>
		implements PractitionerService
{
	public PractitionerServiceImpl(String path, String serverBase, int defaultPageCount, PractitionerDao dao,
			ResourceValidator validator, EventHandler eventHandler, ExceptionHandler exceptionHandler,
			EventGenerator eventGenerator, ResponseGenerator responseGenerator, ParameterConverter parameterConverter,
			ReferenceExtractor referenceExtractor, ReferenceResolver referenceResolver,
			ReferenceCleaner referenceCleaner, AuthorizationRuleProvider authorizationRuleProvider,
			HistoryService historyService)
	{
		super(path, Practitioner.class, serverBase, defaultPageCount, dao, validator, eventHandler, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter, referenceExtractor, referenceResolver,
				referenceCleaner, authorizationRuleProvider, historyService);
	}
}
