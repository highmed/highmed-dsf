package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.LocationDao;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.service.ResourceValidator;
import org.hl7.fhir.r4.model.Location;

@Path(LocationService.RESOURCE_TYPE_NAME)
public class LocationService extends AbstractService<LocationDao, Location>
{
	public static final String RESOURCE_TYPE_NAME = "Location";

	public LocationService(String serverBase, int defaultPageCount, LocationDao locationDao,
			ResourceValidator validator, EventManager eventManager)
	{
		super(serverBase, defaultPageCount, Location.class, locationDao, validator, eventManager);
	}
}
