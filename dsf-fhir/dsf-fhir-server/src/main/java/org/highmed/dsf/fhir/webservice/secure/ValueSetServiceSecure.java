package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.ValueSetAuthorizationRule;
import org.highmed.dsf.fhir.dao.ValueSetDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.ValueSetService;
import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetServiceSecure extends AbstractResourceServiceSecure<ValueSetDao, ValueSet, ValueSetService>
		implements ValueSetService
{
	public ValueSetServiceSecure(ValueSetService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner, ValueSetDao valueSetDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			ValueSetAuthorizationRule authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, ValueSet.class, valueSetDao,
				exceptionHandler, parameterConverter, authorizationRule);
	}
}
