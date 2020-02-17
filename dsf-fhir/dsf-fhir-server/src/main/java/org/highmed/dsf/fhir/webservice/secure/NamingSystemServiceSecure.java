package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.dao.NamingSystemDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.NamingSystemService;
import org.hl7.fhir.r4.model.NamingSystem;

public class NamingSystemServiceSecure extends
		AbstractResourceServiceSecure<NamingSystemDao, NamingSystem, NamingSystemService> implements NamingSystemService
{
	public NamingSystemServiceSecure(NamingSystemService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, NamingSystemDao naminngSystemDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, NamingSystem.class, naminngSystemDao,
				exceptionHandler, parameterConverter);
	}
}
