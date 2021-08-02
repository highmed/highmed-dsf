package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootAuthorizationRule implements AuthorizationRule<Resource>
{
	private static final Logger logger = LoggerFactory.getLogger(RootAuthorizationRule.class);

	@Override
	public Class<Resource> getResourceType()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Resource newResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Resource newResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonReadAllowed(User user, Resource existingResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Resource existingResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Resource oldResource, Resource newResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Resource oldResource,
			Resource newResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(User user, Resource oldResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Resource oldResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("Root History authorized for {} user '{}', will be filtered by user role or users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by user role or users organization");
	}

	@Override
	public Optional<String> reasonPermanentDeleteAllowed(User user, Resource oldResource)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<String> reasonPermanentDeleteAllowed(Connection connection, User user, Resource oldResource)
	{
		throw new UnsupportedOperationException();
	}
}
