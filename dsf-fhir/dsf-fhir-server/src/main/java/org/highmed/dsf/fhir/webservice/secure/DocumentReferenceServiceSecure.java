package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.DocumentReferenceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.DocumentReferenceService;
import org.hl7.fhir.r4.model.DocumentReference;

public class DocumentReferenceServiceSecure
		extends AbstractResourceServiceSecure<DocumentReferenceDao, DocumentReference, DocumentReferenceService>
		implements DocumentReferenceService
{
	public DocumentReferenceServiceSecure(DocumentReferenceService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, DocumentReferenceDao documentReferenceDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			AuthorizationRule<DocumentReference> authorizationRule, ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				DocumentReference.class, documentReferenceDao, exceptionHandler, parameterConverter, authorizationRule,
				resourceValidator);
	}
}
