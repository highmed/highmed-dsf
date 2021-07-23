package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.LocationDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationAuthorizationRule extends AbstractMetaTagAuthorizationRule<Location, LocationDao>
{
	private static final Logger logger = LoggerFactory.getLogger(LocationAuthorizationRule.class);

	public LocationAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider, ReadAccessHelper readAccessHelper)
	{
		super(Location.class, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper);
	}

	protected Optional<String> newResourceOk(Connection connection, User user, Location newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (!hasValidReadAccessTag(connection, newResource))
		{
			errors.add("Location is missing valid read access tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected boolean resourceExists(Connection connection, Location newResource)
	{
		// no unique criteria for Location
		return false;
	}

	@Override
	protected boolean modificationsOk(Connection connection, Location oldResource, Location newResource)
	{
		// no unique criteria for Location
		return true;
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, Location oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Expunge of Location authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of Location unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
