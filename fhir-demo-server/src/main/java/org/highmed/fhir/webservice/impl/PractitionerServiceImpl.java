package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.PractitionerDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.PractitionerService;
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerServiceImpl extends AbstractServiceImpl<PractitionerDao, Practitioner>
		implements PractitionerService
{
	public PractitionerServiceImpl(String serverBase, int defaultPageCount, PractitionerDao practitionerDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<Practitioner> serviceHelper)
	{
		super(serverBase, defaultPageCount, practitionerDao, validator, eventManager, serviceHelper);
	}
}
