package org.highmed.dsf.fhir.authorization;

import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.LocationDao;
import org.hl7.fhir.r4.model.Location;

public class LocationAuthorizationRule extends AbstractAuthorizationRule<Location, LocationDao>
{
	public LocationAuthorizationRule(LocationDao dao)
	{
		super(dao);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Location newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Location existingResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Location oldResource, Location newResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Location oldResource)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
