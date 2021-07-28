package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.NamingSystemService;
import org.hl7.fhir.r4.model.NamingSystem;

public class NamingSystemServiceSecure extends
		AbstractResourceServiceSecure<NamingSystemDao, NamingSystem, NamingSystemService> implements NamingSystemService
{
	public NamingSystemServiceSecure(NamingSystemService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, NamingSystemDao naminngSystemDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, AuthorizationRule<NamingSystem> authorizationRule,
			ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				NamingSystem.class, naminngSystemDao, exceptionHandler, parameterConverter, authorizationRule,
				resourceValidator);
	}
}
