package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.PractitionerRoleDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.PractitionerRoleService;
import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleServiceImpl extends AbstractServiceImpl<PractitionerRoleDao, PractitionerRole>
		implements PractitionerRoleService
{
	public PractitionerRoleServiceImpl(String serverBase, int defaultPageCount, PractitionerRoleDao practitionerRoleDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<PractitionerRole> serviceHelper)
	{
		super(serverBase, defaultPageCount, practitionerRoleDao, validator, eventManager, serviceHelper);
	}
}
