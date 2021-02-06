package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.LocationService;
import org.hl7.fhir.r4.model.Location;

@Path(LocationServiceJaxrs.PATH)
public class LocationServiceJaxrs extends AbstractResourceServiceJaxrs<Location, LocationService>
		implements LocationService
{
	public static final String PATH = "Location";

	public LocationServiceJaxrs(LocationService delegate)
	{
		super(delegate);
	}
}
