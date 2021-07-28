package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.ResearchStudyDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.ResearchStudyService;
import org.hl7.fhir.r4.model.ResearchStudy;

public class ResearchStudyServiceSecure
		extends AbstractResourceServiceSecure<ResearchStudyDao, ResearchStudy, ResearchStudyService>
		implements ResearchStudyService
{
	public ResearchStudyServiceSecure(ResearchStudyService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, ResearchStudyDao researchStudyDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<ResearchStudy> authorizationRule,
			ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				ResearchStudy.class, researchStudyDao, exceptionHandler, parameterConverter, authorizationRule,
				resourceValidator);
	}
}
