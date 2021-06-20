package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.OrganizationAffiliationService;
import org.hl7.fhir.r4.model.OrganizationAffiliation;

@Path(OrganizationAffiliationServiceJaxrs.PATH)
public class OrganizationAffiliationServiceJaxrs
		extends AbstractResourceServiceJaxrs<OrganizationAffiliation, OrganizationAffiliationService>
		implements OrganizationAffiliationService
{
	public static final String PATH = "OrganizationAffiliation";

	public OrganizationAffiliationServiceJaxrs(OrganizationAffiliationService delegate)
	{
		super(delegate);
	}
}
