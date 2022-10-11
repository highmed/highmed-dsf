package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.QuestionnaireResponseDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.QuestionnaireResponseService;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

public class QuestionnaireResponseServiceSecure extends
		AbstractResourceServiceSecure<QuestionnaireResponseDao, QuestionnaireResponse, QuestionnaireResponseService>
		implements QuestionnaireResponseService
{
	public QuestionnaireResponseServiceSecure(QuestionnaireResponseService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, QuestionnaireResponseDao QuestionnaireResponseDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			AuthorizationRule<QuestionnaireResponse> authorizationRule, ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				QuestionnaireResponse.class, QuestionnaireResponseDao, exceptionHandler, parameterConverter,
				authorizationRule, resourceValidator);
	}
}
