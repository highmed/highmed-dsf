package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.OrganizationAuthorizationRule;
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.OrganizationService;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationServiceSecure extends
		AbstractResourceServiceSecure<OrganizationDao, Organization, OrganizationService> implements OrganizationService
{
	public OrganizationServiceSecure(OrganizationService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, OrganizationDao organizationDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			OrganizationAuthorizationRule authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Organization.class, organizationDao,
				exceptionHandler, parameterConverter, authorizationRule);
	}
}
