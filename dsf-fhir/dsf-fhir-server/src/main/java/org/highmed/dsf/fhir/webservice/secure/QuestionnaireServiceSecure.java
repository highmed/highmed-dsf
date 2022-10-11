package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.QuestionnaireDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.QuestionnaireService;
import org.hl7.fhir.r4.model.Questionnaire;

public class QuestionnaireServiceSecure
		extends AbstractResourceServiceSecure<QuestionnaireDao, Questionnaire, QuestionnaireService>
		implements QuestionnaireService
{
	public QuestionnaireServiceSecure(QuestionnaireService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, QuestionnaireDao questionnaireDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<Questionnaire> authorizationRule,
			ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				Questionnaire.class, questionnaireDao, exceptionHandler, parameterConverter, authorizationRule,
				resourceValidator);
	}
}
