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
import org.highmed.dsf.fhir.dao.OrganizationDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.PartialResult;
import org.highmed.dsf.fhir.search.SearchQuery;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationAuthorizationRule extends AbstractAuthorizationRule<Organization, OrganizationDao>
{
	private static final Logger logger = LoggerFactory.getLogger(OrganizationAuthorizationRule.class);

	private static final String IDENTIFIER_SYSTEM = "http://highmed.org/fhir/NamingSystem/organization-identifier";
	private static final String EXTENSION_THUMBPRINT_URL = "http://highmed.org/fhir/StructureDefinition/extension-certificate-thumbprint";
	private static final String EXTENSION_THUMBPRINT_VALUE_PATTERN_STRING = "[a-z0-9]{128}";
	private static final Pattern EXTENSION_THUMBPRINT_VALUE_PATTERN = Pattern
			.compile(EXTENSION_THUMBPRINT_VALUE_PATTERN_STRING);

	public OrganizationAuthorizationRule(DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider)
	{
		super(Organization.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Organization newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Create of Organization authorized for local user '{}', Organization with certificate-thumbprint and identifier does not exist",
							user.getName());
					return Optional
							.of("local user, Organization with certificate-thumbprint and identifier does not exist");
				}
				else
				{
					logger.warn(
							"Create of Organization unauthorized, Organization with certificate-thumbprint and identifier already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of Organization unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of Organization unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private Optional<String> newResourceOk(Organization newResource)
	{
		List<String> errors = new ArrayList<String>();

		if (newResource.hasIdentifier())
		{
			if (newResource.getIdentifier().stream()
					.filter(i -> i.hasSystem() && i.hasValue() && IDENTIFIER_SYSTEM.equals(i.getSystem())).count() != 1)
			{
				errors.add("Organization.identifier one with system '" + IDENTIFIER_SYSTEM
						+ "' and non empty value expected");
			}
		}
		else
		{
			errors.add("Organization.identifier missing");
		}

		if (newResource.hasExtension())
		{
			if (!newResource.getExtension().stream().anyMatch(e -> e.hasUrl() && e.hasValue()
					&& (e.getValue() instanceof StringType) && EXTENSION_THUMBPRINT_URL.equals(e.getUrl())
					&& EXTENSION_THUMBPRINT_VALUE_PATTERN.matcher(((StringType) e.getValue()).getValue()).matches()))
			{
				errors.add("Organization.extension missing extension with url '" + EXTENSION_THUMBPRINT_URL
						+ "' and value matching " + EXTENSION_THUMBPRINT_VALUE_PATTERN_STRING + " pattern");
			}
		}
		else
		{
			errors.add("Organization.extension missing");
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

	private boolean resourceExists(Connection connection, Organization newResource)
	{
		String identifierValue = newResource.getIdentifier().stream()
				.filter(i -> i.hasSystem() && i.hasValue() && IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.map(i -> i.getValue()).findFirst().orElseThrow();

		return resourceExistsWithThumbprint(connection, newResource)
				|| organizationWithIdentifierExists(connection, identifierValue);
	}

	private boolean resourceExistsWithThumbprint(Connection connection, Organization newResource)
	{
		String thumbprintValue = newResource.getExtension().stream()
				.filter(e -> e.hasUrl() && e.hasValue() && (e.getValue() instanceof StringType)
						&& EXTENSION_THUMBPRINT_URL.equals(e.getUrl()))
				.map(e -> ((StringType) e.getValue()).getValue()).findFirst().orElseThrow();

		return organizationWithThumbprintExists(connection, thumbprintValue);
	}

	private boolean organizationWithThumbprintExists(Connection connection, String thumbprintHex)
	{
		try
		{
			OrganizationDao dao = getDao();
			return dao.existsNotDeletedByThumbprintWithTransaction(connection, thumbprintHex);
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Endpoint with thumbprint", e);
			return false;
		}
	}

	private boolean organizationWithIdentifierExists(Connection connection, String identifierValue)
	{
		Map<String, List<String>> queryParameters = Map.of("identifier",
				Collections.singletonList(IDENTIFIER_SYSTEM + "|" + identifierValue));
		OrganizationDao dao = getDao();
		SearchQuery<Organization> query = dao.createSearchQueryWithoutUserFilter(0, 0)
				.configureParameters(queryParameters);

		if (!query.getUnsupportedQueryParameters(queryParameters).isEmpty())
			return false;

		try
		{
			PartialResult<Organization> result = dao.searchWithTransaction(connection, query);
			return result.getTotal() >= 1;
		}
		catch (SQLException e)
		{
			logger.warn("Error while searching for Endpoint with identifier", e);
			return false;
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Organization existingResource)
	{
		if (isLocalUser(user) && hasLocalOrRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of Organization authorized for local user '{}', Organization has local or remote authorization role",
					user.getName());
			return Optional.of("local user, local or remote authorized Organization");
		}
		else if (isRemoteUser(user) && hasRemoteAuthorizationRole(existingResource))
		{
			logger.info(
					"Read of Organization authorized for remote user '{}', Organization has remote authorization role",
					user.getName());
			return Optional.of("remote user, remote authorized Organization");
		}
		else
		{
			logger.warn("Read of Organization unauthorized, no matching user role resource authorization role found");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Organization oldResource,
			Organization newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(newResource);
			if (errors.isEmpty())
			{
				if (isIdentifierSame(oldResource, newResource) && (isThumbprintSame(oldResource, newResource)
						|| !resourceExistsWithThumbprint(connection, newResource)))
				{
					logger.info(
							"Update of Organization authorized for local user '{}', identifier same as existing Organization and certificate-thumbprint same as existing or other Organization with thumbprint does not exist",
							user.getName());
					return Optional.of(
							"local user; identifier same as existing Organization and certificate-thumbprint same as existing or other Organization with thumbprint does not exist");
				}
				else if (!resourceExists(connection, newResource))
				{
					logger.info(
							"Update of Organization authorized for local user '{}', other Organization with certificate-thumbprint or identifier does not exist",
							user.getName());
					return Optional.of(
							"local user; other Organization with certificate-thumbprint and identifier does not exist");
				}
				else
				{
					logger.warn(
							"Update of Organization unauthorized, other Organization with certificate-thumbprint and identifier already exists");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of Organization unauthorized, " + errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of Organization unauthorized, not a local user");
			return Optional.empty();
		}
	}

	private boolean isThumbprintSame(Organization oldResource, Organization newResource)
	{
		String oldThumbprintValue = oldResource.getExtension().stream()
				.filter(e -> EXTENSION_THUMBPRINT_URL.equals(e.getUrl()))
				.map(e -> ((StringType) e.getValue()).getValue()).findFirst().orElseThrow();

		String newThumbprintValue = newResource.getExtension().stream()
				.filter(e -> EXTENSION_THUMBPRINT_URL.equals(e.getUrl()))
				.map(e -> ((StringType) e.getValue()).getValue()).findFirst().orElseThrow();

		return oldThumbprintValue.equals(newThumbprintValue);
	}

	private boolean isIdentifierSame(Organization oldResource, Organization newResource)
	{
		String oldIdentifierValue = oldResource.getIdentifier().stream()
				.filter(i -> IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst().orElseThrow();

		String newIdentifierValue = newResource.getIdentifier().stream()
				.filter(i -> IDENTIFIER_SYSTEM.equals(i.getSystem())).map(i -> i.getValue()).findFirst().orElseThrow();

		return oldIdentifierValue.equals(newIdentifierValue);
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Organization oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of Organization authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of Organization unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of Organization authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of Organization authorized for {} user '{}', will be fitered by user role", user.getRole(),
				user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, Organization oldResource) {
		if (isLocalUser(user))
		{
			logger.info("Expunge of Organization authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of Organization unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
