package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.ProvenanceAuthorizationRule;
import org.highmed.dsf.fhir.dao.ProvenanceDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.ProvenanceService;
import org.hl7.fhir.r4.model.Provenance;

public class ProvenanceServiceSecure extends AbstractResourceServiceSecure<ProvenanceDao, Provenance, ProvenanceService>
		implements ProvenanceService
{
	public ProvenanceServiceSecure(ProvenanceService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, ProvenanceDao provenanceDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, ProvenanceAuthorizationRule authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Provenance.class, provenanceDao,
				exceptionHandler, parameterConverter, authorizationRule);
	}
}
