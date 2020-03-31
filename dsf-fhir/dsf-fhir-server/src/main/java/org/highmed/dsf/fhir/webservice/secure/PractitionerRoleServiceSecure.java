package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.authorization.PractitionerRoleAuthorizationRule;
import org.highmed.dsf.fhir.dao.PractitionerRoleDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleServiceSecure
		extends AbstractResourceServiceSecure<PractitionerRoleDao, PractitionerRole, PractitionerRoleService>
		implements PractitionerRoleService
{
	public PractitionerRoleServiceSecure(PractitionerRoleService delegate, String serverBase,
			ResponseGenerator responseGenerator, ReferenceResolver referenceResolver,
			PractitionerRoleDao practitionerRoleDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter, PractitionerRoleAuthorizationRule authorizationRule)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, PractitionerRole.class, practitionerRoleDao,
				exceptionHandler, parameterConverter, authorizationRule);
	}
}
