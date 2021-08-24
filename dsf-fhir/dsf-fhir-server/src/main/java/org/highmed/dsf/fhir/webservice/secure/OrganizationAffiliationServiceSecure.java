package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.AuthorizationRule;
import org.highmed.dsf.fhir.dao.OrganizationAffiliationDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceCleaner;
import org.highmed.dsf.fhir.service.ReferenceExtractor;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.webservice.specification.OrganizationAffiliationService;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

public class OrganizationAffiliationServiceSecure extends
		AbstractResourceServiceSecure<OrganizationAffiliationDao, OrganizationAffiliation, OrganizationAffiliationService>
		implements OrganizationAffiliationService
{
	public OrganizationAffiliationServiceSecure(OrganizationAffiliationService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver, ReferenceCleaner referenceCleaner,
			ReferenceExtractor referenceExtractor, OrganizationAffiliationDao organizationDao,
			ExceptionHandler exceptionHandler, ParameterConverter parameterConverter,
			AuthorizationRule<OrganizationAffiliation> authorizationRule, ResourceValidator resourceValidator)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, referenceCleaner, referenceExtractor,
				OrganizationAffiliation.class, organizationDao, exceptionHandler, parameterConverter, authorizationRule,
				resourceValidator);
	}
}
