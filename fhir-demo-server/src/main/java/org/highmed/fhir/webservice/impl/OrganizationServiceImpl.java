package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.OrganizationDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.OrganizationService;
import org.hl7.fhir.r4.model.Organization;

public class OrganizationServiceImpl extends AbstractServiceImpl<OrganizationDao, Organization>
		implements OrganizationService
{
	public OrganizationServiceImpl(String serverBase, int defaultPageCount, OrganizationDao organizationDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<Organization> serviceHelper)
	{
		super(serverBase, defaultPageCount, organizationDao, validator, eventManager, serviceHelper);
	}
}
