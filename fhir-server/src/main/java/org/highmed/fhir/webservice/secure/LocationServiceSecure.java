package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.LocationService;
import org.hl7.fhir.r4.model.Location;

public class LocationServiceSecure extends AbstractServiceSecure<Location, LocationService> implements LocationService
{
	public LocationServiceSecure(LocationService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
