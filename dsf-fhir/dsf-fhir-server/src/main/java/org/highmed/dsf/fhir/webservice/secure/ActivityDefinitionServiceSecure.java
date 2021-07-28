package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.ActivityDefinitionDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.ActivityDefinitionService;
import org.hl7.fhir.r4.model.ActivityDefinition;

public class ActivityDefinitionServiceSecure
		extends AbstractResourceServiceSecure<ActivityDefinitionDao, ActivityDefinition, ActivityDefinitionService>
		implements ActivityDefinitionService
{
	public ActivityDefinitionServiceSecure(ActivityDefinitionService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, ActivityDefinitionDao activityDefinitionDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			AuthorizationRule<ActivityDefinition> authorizationRule, ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				ActivityDefinition.class, activityDefinitionDao, exceptionHandler, parameterConverter,
				authorizationRule, resourceValidator);
	}
}
