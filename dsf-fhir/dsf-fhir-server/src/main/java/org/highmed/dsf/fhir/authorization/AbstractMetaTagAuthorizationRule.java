package org.highmed.dsf.fhir.authorization;

import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ALL;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_LOCAL;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ORGANIZATION;
import static org.highmed.dsf.fhir.authorization.read.ReadAccessHelper.READ_ACCESS_TAG_VALUE_ROLE;

import java.sql.Connection;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

public abstract class AbstractMetaTagAuthorizationRule<R extends Resource, D extends ResourceDao<R>>
		extends AbstractAuthorizationRule<R, D> implements AuthorizationRule<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractMetaTagAuthorizationRule.class);

	private final String resourceTypeName;

	public AbstractMetaTagAuthorizationRule(Class<R> resourceType, DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper)
	{
		super(resourceType, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper);

		this.resourceTypeName = resourceType.getAnnotation(ResourceDef.class).name();
	}

	protected final boolean hasValidReadAccessTag(Connection connection, Resource resource)
	{
		return readAccessHelper.isValid(resource,
				organizationIdentifier -> organizationWithIdentifierExists(connection, organizationIdentifier),
				role -> roleExists(connection, role));
	}

	@Override
	public final Optional<String> reasonCreateAllowed(Connection connection, User user, R newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource);
			if (errors.isEmpty())
			{
				if (!resourceExists(connection, newResource))
				{
					logger.info("Create of {} authorized for local user '{}', {} does not exist", resourceTypeName,
							user.getName(), resourceTypeName);
					return Optional.of("local user, " + resourceTypeName + " does not exist yet");
				}
				else
				{
					logger.warn("Create of {} unauthorized, {} already exists", resourceTypeName, resourceTypeName);
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of {} unauthorized, {}", resourceTypeName, errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of {} unauthorized, not a local user", resourceTypeName);
			return Optional.empty();
		}
	}

	protected abstract boolean resourceExists(Connection connection, R newResource);

	protected abstract Optional<String> newResourceOk(Connection connection, User user, R newResource);

	@Override
	public final Optional<String> reasonReadAllowed(Connection connection, User user, R existingResource)
	{
		if (isLocalUser(user) && readAccessHelper.hasLocal(existingResource))
		{
			logger.info("Read of {} authorized for local user '{}', {} has '{}' read access tag", resourceTypeName,
					user.getName(), resourceTypeName, READ_ACCESS_TAG_VALUE_LOCAL);
			return Optional
					.of("local user, '" + READ_ACCESS_TAG_VALUE_LOCAL + "' read access tag on " + resourceTypeName);
		}
		else if (isLocalUser(user) && readAccessHelper.hasAll(existingResource))
		{
			logger.info("Read of {} authorized for local user '{}', {} has '{}' read access tag", resourceTypeName,
					user.getName(), resourceTypeName, READ_ACCESS_TAG_VALUE_ALL);
			return Optional
					.of("local user, '" + READ_ACCESS_TAG_VALUE_ALL + "' read access tag on " + resourceTypeName);
		}
		else if (isRemoteUser(user) && readAccessHelper.hasAll(existingResource))
		{
			logger.info("Read of {} authorized for remote user '{}', {} has '{}' read access tag", resourceTypeName,
					user.getName(), resourceTypeName, READ_ACCESS_TAG_VALUE_ALL);
			return Optional
					.of("remote user, '" + READ_ACCESS_TAG_VALUE_ALL + "' read access tag on " + resourceTypeName);
		}
		else if (isLocalUser(user) && readAccessHelper.hasAnyOrganization(existingResource)
				&& readAccessHelper.hasOrganization(existingResource, user.getOrganization()))
		{
			logger.info("Read of {} authorized for local user '{}', {} has '{}' read access tag", resourceTypeName,
					user.getName(), resourceTypeName, READ_ACCESS_TAG_VALUE_ORGANIZATION);
			return Optional.of(
					"remote user, '" + READ_ACCESS_TAG_VALUE_ORGANIZATION + "' read access tag on " + resourceTypeName);
		}
		else if (isRemoteUser(user) && readAccessHelper.hasAnyOrganization(existingResource)
				&& readAccessHelper.hasOrganization(existingResource, user.getOrganization()))
		{
			logger.info("Read of {} authorized for remote user '{}', {} has '{}' read access tag", resourceTypeName,
					user.getName(), resourceTypeName, READ_ACCESS_TAG_VALUE_ORGANIZATION);
			return Optional.of(
					"remote user, '" + READ_ACCESS_TAG_VALUE_ORGANIZATION + "' read access tag on " + resourceTypeName);
		}
		else if (isLocalUser(user) && readAccessHelper.hasAnyRole(existingResource)
				&& readAccessHelper.hasRole(existingResource, getAffiliations(connection, user)))
		{
			logger.info("Read of {} authorized for local user '{}', {} has '{}' read access tag", resourceTypeName,
					user.getName(), resourceTypeName, READ_ACCESS_TAG_VALUE_ROLE);
			return Optional
					.of("remote user, '" + READ_ACCESS_TAG_VALUE_ROLE + "' read access tag on " + resourceTypeName);
		}
		else if (isRemoteUser(user) && readAccessHelper.hasAnyRole(existingResource)
				&& readAccessHelper.hasRole(existingResource, getAffiliations(connection, user)))
		{
			logger.info("Read of {} authorized for remote user '{}', {} has '{}' read access tag", resourceTypeName,
					user.getName(), resourceTypeName, READ_ACCESS_TAG_VALUE_ROLE);
			return Optional
					.of("remote user, '" + READ_ACCESS_TAG_VALUE_ROLE + "' read access tag on " + resourceTypeName);
		}
		else
		{
			Optional<String> allowed = reasonReadAllowedByAdditionalCriteria(connection, user, existingResource);
			if (allowed.isPresent())
				return allowed;
			else
			{
				logger.warn("Read of {} unauthorized", resourceTypeName);
				return Optional.empty();
			}
		}
	}

	protected Optional<String> reasonReadAllowedByAdditionalCriteria(Connection connection, User user,
			R existingResource)
	{
		return Optional.empty();
	}

	@Override
	public final Optional<String> reasonUpdateAllowed(Connection connection, User user, R oldResource, R newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOk(connection, user, newResource);
			if (errors.isEmpty())
			{
				if (modificationsOk(connection, oldResource, newResource))
				{
					logger.info("Update of {} authorized for local user '{}', modification allowed", resourceTypeName,
							user.getName());
					return Optional.of("local user; modification allowed");
				}
				else
				{
					logger.warn("Update of {} unauthorized, modification not allowed", resourceTypeName);
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of {} unauthorized, ", resourceTypeName, errors.get());
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of {} unauthorized, not a local user", resourceTypeName);
			return Optional.empty();
		}
	}

	/**
	 * No need to check if the new resource is valid, will be checked by
	 * {@link #newResourceOk(Connection, User, Resource)}
	 * 
	 * @param connection
	 *            not <code>null</code>
	 * @param oldResource
	 *            not <code>null</code>
	 * @param newResource
	 *            not <code>null</code>
	 * @return <code>true</code> if modifications from <b>oldResource</b> to <b>newResource</b> are ok
	 */
	protected abstract boolean modificationsOk(Connection connection, R oldResource, R newResource);

	@Override
	public final Optional<String> reasonDeleteAllowed(Connection connection, User user, R oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of {} authorized for local user '{}'", resourceTypeName, user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of {} unauthorized, not a local user", resourceTypeName);
			return Optional.empty();
		}
	}

	@Override
	public final Optional<String> reasonSearchAllowed(User user)
	{
		logger.info("Search of {} authorized for {} user '{}', will be filtered by users organization and roles",
				resourceTypeName, user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public final Optional<String> reasonHistoryAllowed(User user)
	{
		logger.info("History of {} authorized for {} user '{}', will be filtered by users organization and roles",
				resourceTypeName, user.getRole(), user.getName());
		return Optional.of("Allowed for all, filtered by user role");
	}

	@Override
	public Optional<String> reasonExpungeAllowed(Connection connection, User user, R oldResource)
	{
		if (isLocalDeletionUser(user))
		{
			logger.info("Expunge of {} authorized for local delete user '{}'", resourceType, user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Expunge of {} unauthorized, not a local delete user", resourceTypeName);
			return Optional.empty();
		}
	}
}
