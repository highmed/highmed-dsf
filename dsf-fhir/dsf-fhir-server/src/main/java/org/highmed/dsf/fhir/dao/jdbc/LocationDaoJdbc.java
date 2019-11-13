package org.highmed.dsf.fhir.dao.jdbc;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.dao.LocationDao;
import org.highmed.dsf.fhir.search.parameters.LocationIdentifier;
import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;

public class LocationDaoJdbc extends AbstractResourceDaoJdbc<Location> implements LocationDao
{
	public LocationDaoJdbc(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Location.class, "locations", "location", "location_id", LocationIdentifier::new);
	}

	@Override
	protected Location copy(Location resource)
	{
		return resource.copy();
	}
}
