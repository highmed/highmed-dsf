package org.highmed.fhir.webservice;

import javax.ws.rs.Path;

import org.highmed.fhir.dao.LocationDao;
import org.hl7.fhir.r4.model.Location;

@Path(LocationService.RESOURCE_TYPE_NAME)
public class LocationService extends AbstractService<LocationDao, Location>
{
	public static final String RESOURCE_TYPE_NAME = "Location";

	public LocationService(String serverBase, LocationDao locationDao)
	{
		super(serverBase, RESOURCE_TYPE_NAME, locationDao);
	}
}
