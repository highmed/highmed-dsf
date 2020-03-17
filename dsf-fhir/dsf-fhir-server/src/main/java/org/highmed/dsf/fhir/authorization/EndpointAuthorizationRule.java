package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.EndpointDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointAuthorizationRule extends AbstractAuthorizationRule<Endpoint, EndpointDao>
{
	private static final Logger logger = LoggerFactory.getLogger(EndpointAuthorizationRule.class);

	private static final String IDENTIFIER_SYSTEM = "http://highmed.org/fhir/NamingSystem/endpoint-identifier";
	private static final String ENDPOINT_ADDRESS_PATTERN_STRING = "https://([0-9a-zA-Z\\.]+)+(:\\d{1,4})?([-\\w/]*)";
	private static final Pattern ENDPOINT_ADDRESS_PATTERN = Pattern.compile(ENDPOINT_ADDRESS_PATTERN_STRING);

	public EndpointAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider)
	{
		super(Endpoint.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Endpoint newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Create of Endpoint authorized for local user '{}', Endpoint with address and identifier does not exist",
							user.getName());
					return Optional.of("local user, Endpoint with address and identifier does not exist yet");
				}
				else
				{
					logger.warn("Create of Endpoint unauthorized, Endpoint with address and identifier already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of Endpoint unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of Endpoint unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(Endpoint newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasIdentifier())
		{
			if (newResource.getIdentifier().stream()
					.filter(i -> i.hasSystem() && i.hasValue() && IDENTIFIER_SYSTEM.equals(i.getSystem())).count() != 1)
			{
				errors.add(
						"Endpoint.identifier one with system '" + IDENTIFIER_SYSTEM + "' and non empty value expected");
			}
		}
		else
		{
			errors.add("Endpoint.identifier missing");
		}

		if (newResource.hasAddress())
		{
			if (!ENDPOINT_ADDRESS_PATTERN.matcher(newResource.getAddress()).matches())
			{
				errors.add("Endpoint.address not matching " + ENDPOINT_ADDRESS_PATTERN_STRING + " pattern");
			}
		}
		else
		{
			errors.add("Endpoint.address missing");
		}

		if (!hasLocalOrRemoteAuthorizationRole(newResource))
		{
			errors.add("missing authorization tag");
		}

		if (errors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(errors.stream().collect(Collectors.joining(", ")));
	}

	private boolean resourceExists(Connection connection, Endpoint newResource)
	{
		String identifierValue = newResource.getIdentifier().stream()
				.filter(i -> i.hasSystem() && i.hasValue() && IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.map(i -> i.getValue()).findFirst().orElseThrow();

		return endpointWithAddressExists(connection, newResource.getAddress())
				|| endpointWithIdentifierExists(connection, identifierValue);
	}

	private boolean endpointWithAddressExists(Connection connection, String address)
	{
		Map<String, List<String>> queryParameters = Map.of("address", Collections.singletonList(address));
		EndpointDao dao = getDao();
		SearchQuery<Endpoint> query = dao.createSearchQueryWithoutUserFilter(0, 0).configureParameters(queryParameters);

		if (!query.getUnsupportedQueryParameters(queryParameters).isEmpty())
			return false;

		try
		{
			PartialResult<Endpoint> result = dao.searchWithTransaction(connection, query);
			return result.getOverallCount() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Endpoint with address", e);
			return false;
		}
	}

	private boolean endpointWithIdentifierExists(Connection connection, String identifierValue)
	{
		Map<String, List<String>> queryParameters = Map.of("identifier",
				Collections.singletonList(IDENTIFIER_SYSTEM + "|" + identifierValue));
		EndpointDao dao = getDao();
		SearchQuery<Endpoint> query = dao.createSearchQueryWithoutUserFilter(0, 0).configureParameters(queryParameters);

		if (!query.getUnsupportedQueryParameters(queryParameters).isEmpty())
			return false;

		try
		{
			PartialResult<Endpoint> result = dao.searchWithTransaction(connection, query);
			return result.getOverallCount() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Endpoint with identifier", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Endpoint existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of Endpoint authorized for local user '{}', Endpoint has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized Endpoint");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info("Read of Endpoint authorized for remote user '{}', Endpoint has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized Endpoint");
		}
		else
		{
			logger.warn("Read of Endpoint unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Endpoint oldResource,
			Endpoint newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (isSame(oldResource, newResource))
				{
					logger.info(
							"Update of Endpoint authorized for local user '{}', address and identifier same as existing Endpoint",
							user.getName());
					return Optional.of("local user; address and identifier same as existing Endpoint");

				}
				else if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Update of Endpoint authorized for local user '{}', other Endpoint with address and identifier does not exist",
							user.getName());
					return Optional.of("local user; other Endpoint with address and identifier does not exist yet");
				}
				else
				{
					logger.warn(
							"Update of Endpoint unauthorized, other Endpoint with address and identifier already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of Endpoint unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of Endpoint unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean isSame(Endpoint oldResource, Endpoint newResource)
	{
		String oldIdentifierValue = oldResource.getIdentifier().stream()
				.filter(i -> IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst().orElseThrow();
		String newIdentifierValue = newResource.getIdentifier().stream()
				.filter(i -> IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst().orElseThrow();

		return oldResource.getAddress().equals(newResource.getAddress())
				&& oldIdentifierValue.equals(newIdentifierValue);
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Endpoint oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of Endpoint authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of Endpoint unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of Endpoint authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}
}
