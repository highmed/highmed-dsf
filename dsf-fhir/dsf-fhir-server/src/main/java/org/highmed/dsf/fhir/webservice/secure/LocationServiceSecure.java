package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.LocationService;
import org.hl7.fhir.r4.model.Location;

public class LocationServiceSecure extends AbstractServiceSecure<Location, LocationService> implements LocationService
{
	public LocationServiceSecure(LocationService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
