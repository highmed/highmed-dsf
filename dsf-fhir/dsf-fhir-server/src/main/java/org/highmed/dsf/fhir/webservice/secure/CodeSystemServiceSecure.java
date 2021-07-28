package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.CodeSystemDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.CodeSystemService;
import org.hl7.fhir.r4.model.CodeSystem;

public class CodeSystemServiceSecure extends AbstractResourceServiceSecure<CodeSystemDao, CodeSystem, CodeSystemService>
		implements CodeSystemService
{
	public CodeSystemServiceSecure(CodeSystemService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, CodeSystemDao codeSystemDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<CodeSystem> authorizationRule,
			ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				CodeSystem.class, codeSystemDao, exceptionHandler, parameterConverter, authorizationRule,
				resourceValidator);
	}
}
