package org.highmed.fhir.dao;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.fhir.dao.search.PartialResult;
import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.context.FhirContext;

public class LocationDao extends AbstractDao<Location>
{
	public LocationDao(BasicDataSource dataSource, FhirContext fhirContext)
	{
		super(dataSource, fhirContext, Location.class, "locations", "location", "location_id");
	}

	@Override
	protected Location copy(Location resource)
	{
		return resource.copy();
	}

	public PartialResult<Location> search(int page, int count) throws SQLException
	{
		return search(createSearchQueryFactory(page, count).build());
	}
}
