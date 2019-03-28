package org.highmed.fhir.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.search.parameters.LocationIdentifier;
import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;

public class LocationDao extends AbstractDomainResourceDao<Location>
{
	public LocationDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Location.class, "locations", "location", "location_id", LocationIdentifier::new);
	}

	@Override
	protected Location copy(Location resource)
	{
		return resource.copy();
	}
}
