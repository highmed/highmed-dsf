package org.highmed.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import org.highmed.dsf.fhir.dao.jdbc.LocationDaoJdbc;
import org.hl7.fhir.r4.model.Location;

public class LocationDaoTest extends AbstractResourceDaoTest<Location, LocationDao>
{
	private static final String name = "Demo Location";
	private static final String description = "Demo Location Description";

	public LocationDaoTest()
	{
		super(Location.class, LocationDaoJdbc::new);
	}

	@Override
	protected Location createResource()
	{
		Location location = new Location();
		location.setName(name);
		return location;
	}

	@Override
	protected void checkCreated(Location resource)
	{
		assertEquals(name, resource.getName());
	}

	@Override
	protected Location updateResource(Location resource)
	{
		resource.setDescription(description);
		return resource;
	}

	@Override
	protected void checkUpdates(Location resource)
	{
		assertEquals(description, resource.getDescription());
	}
}
