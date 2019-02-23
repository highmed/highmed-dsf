package org.highmed.fhir.webservice.impl;

import org.highmed.fhir.dao.LocationDao;
import org.highmed.fhir.event.EventGenerator;
import org.highmed.fhir.event.EventManager;
import org.highmed.fhir.help.ExceptionHandler;
import org.highmed.fhir.help.ParameterConverter;
import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.service.ResourceValidator;
import org.highmed.fhir.webservice.specification.LocationService;
import org.hl7.fhir.r4.model.Location;

public class LocationServiceImpl extends AbstractServiceImpl<LocationDao, Location> implements LocationService
{
	public LocationServiceImpl(String resourceTypeName, String serverBase, int defaultPageCount, LocationDao dao,
			ResourceValidator validator, EventManager eventManager, ExceptionHandler exceptionHandler,
			EventGenerator<Location> eventGenerator, ResponseGenerator responseGenerator,
			ParameterConverter parameterConverter)
	{
		super(resourceTypeName, serverBase, defaultPageCount, dao, validator, eventManager, exceptionHandler,
				eventGenerator, responseGenerator, parameterConverter);
	}
}
