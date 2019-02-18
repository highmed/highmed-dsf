package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.LocationDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.LocationService;
import org.hl7.fhir.r4.model.Location;

public class LocationServiceImpl extends AbstractServiceImpl<LocationDao, Location> implements LocationService
{
	public LocationServiceImpl(String serverBase, int defaultPageCount, LocationDao locationDao,
			ResourceValidator validator, EventManager eventManager, ServiceHelperImpl<Location> serviceHelper)
	{
		super(serverBase, defaultPageCount, locationDao, validator, eventManager, serviceHelper);
	}
}
