package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.dao.LocationDao;
import org.highmed.dsf.fhir.help.ExceptionHandler;
import org.highmed.dsf.fhir.help.ParameterConverter;
import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.webservice.specification.LocationService;
import org.hl7.fhir.r4.model.Location;

public class LocationServiceSecure extends AbstractResourceServiceSecure<LocationDao, Location, LocationService> implements LocationService
{
	public LocationServiceSecure(LocationService delegate, String serverBase, ResponseGenerator responseGenerator,
			ReferenceResolver referenceResolver, LocationDao locationDao, ExceptionHandler exceptionHandler,
			ParameterConverter parameterConverter)
	{
		super(delegate, serverBase, responseGenerator, referenceResolver, Location.class, locationDao, exceptionHandler,
				parameterConverter);
	}
}
