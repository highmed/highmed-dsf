package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.BinaryAuthorizationRule;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.BinaryService;
import org.hl7.fhir.r4.model.Binary;

public class BinaryServiceSecure extends AbstractResourceServiceSecure<BinaryDao, Binary, BinaryService>
		implements BinaryService
{
	public BinaryServiceSecure(BinaryService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, BinaryDao binaryDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, BinaryAuthorizationRule authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Binary.class, binaryDao, exceptionHandler,
				parameterConverter, authorizationRule);
	}
}