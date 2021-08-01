package org.highmed.dsf.fhir.dao.jdbc;

import javax.sql.DataSource;

import org.highmed.dsf.fhir.dao.LocationDao;
import org.highmed.dsf.fhir.search.parameters.LocationIdentifier;
import org.highmed.dsf.fhir.search.parameters.user.LocationUserFilter;
import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;

public class LocationDaoJdbc extends AbstractResourceDaoJdbc<Location> implements LocationDao
{
	public LocationDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, Location.class, "locations", "location",
				"location_id", LocationUserFilter::new, with(LocationIdentifier::new), with());
	}

	@Override
	protected Location copy(Location resource)
	{
		return resource.copy();
	}
}
