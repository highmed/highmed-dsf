package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.webservice.specification.OrganizationService;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationServiceSecure extends AbstractServiceSecure<Organization, OrganizationService>
		implements OrganizationService
{
	public OrganizationServiceSecure(OrganizationService delegate)
	{
		super(delegate);
	}
}
