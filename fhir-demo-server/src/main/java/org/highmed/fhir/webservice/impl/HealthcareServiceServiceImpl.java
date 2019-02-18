package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.HealthcareServiceDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.HealthcareServiceService;
import org.hl7.fhir.r4.model.HealthcareService;

public class HealthcareServiceServiceImpl extends AbstractServiceImpl<HealthcareServiceDao, HealthcareService>
		implements HealthcareServiceService
{
	public HealthcareServiceServiceImpl(String serverBase, int defaultPageCount,
			HealthcareServiceDao healthcareServiceDao, ResourceValidator validator, EventManager eventManager,
			ServiceHelperImpl<HealthcareService> serviceHelper)
	{
		super(serverBase, defaultPageCount, healthcareServiceDao, validator, eventManager, serviceHelper);
	}
}
