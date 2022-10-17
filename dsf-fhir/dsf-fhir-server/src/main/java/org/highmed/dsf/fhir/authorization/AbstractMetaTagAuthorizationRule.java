package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.dao.ReadAccessDao;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.help.ParameterConverter;
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

	private final ParameterConverter parameterConverter;
	private final ReadAccessDao readAccessDao;
	private final String resourceTypeName;

	public AbstractMetaTagAuthorizationRule(Class<R> resourceType, DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver, OrganizationProvider organizationProvider,
			ReadAccessHelper readAccessHelper, ParameterConverter parameterConverter)
	{
		super(resourceType, daoProvider, serverBase, referenceResolver, organizationProvider, readAccessHelper);

		this.parameterConverter = parameterConverter;

		readAccessDao = daoProvider.getReadAccessDao();
		resourceTypeName = resourceType.getAnnotation(ResourceDef.class).name();
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(parameterConverter, "parameterConverter");
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
			Optional<String> errors = newResourceOkForCreate(connection, user, newResource);
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

	protected abstract Optional<String> newResourceOkForCreate(Connection connection, User user, R newResource);

	@Override
	public final Optional<String> reasonReadAllowed(Connection connection, User user, R existingResource)
	{
		UserRole userRole = user.getRole();
		UUID resourceId = parameterConverter.toUuid(resourceTypeName, existingResource.getIdElement().getIdPart());
		long resourceVersion = existingResource.getIdElement().getVersionIdPartAsLong();
		UUID organizationId = parameterConverter.toUuid("Organization",
				user.getOrganization().getIdElement().getIdPart());

		try
		{
			List<String> accessTypes = readAccessDao.getAccessTypes(connection, resourceId, resourceVersion, userRole,
					organizationId);

			if (accessTypes.isEmpty())
			{
				logger.warn("Read of {}/{} unauthorized", resourceTypeName, resourceId.toString());
				return Optional.empty();
			}
			else
			{
				logger.info("Read of {}/{} authorized for {} user '{}': {}", resourceTypeName, resourceId, userRole,
						user.getName(), accessTypes);
				return Optional.of(userRole + " user, " + (accessTypes.size() > 1 ? "{" : "")
						+ accessTypes.stream().collect(Collectors.joining(", "))
						+ (accessTypes.size() > 1 ? "} tags" : " tag") + " on resource");
			}
		}
		catch (SQLException e)
		{
			logger.warn("Error while checking read access", e);
			throw new RuntimeException(e);
		}
	}

	protected abstract Optional<String> newResourceOkForUpdate(Connection connection, User user, R newResource);

	@Override
	public final Optional<String> reasonUpdateAllowed(Connection connection, User user, R oldResource, R newResource)
	{
		if (isLocalUser(user))
		{
			Optional<String> errors = newResourceOkForUpdate(connection, user, newResource);
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
				logger.warn("Update of {} unauthorized, {}", resourceTypeName, errors.get());
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
	 * {@link #newResourceOkForUpdate(Connection, User, Resource)}
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
	public Optional<String> reasonPermanentDeleteAllowed(Connection connection, User user, R oldResource)
	{
		if (isLocalPermanentDeleteUser(user))
		{
			logger.info("Permanent delete of {} authorized for local delete user '{}'", resourceType, user.getName());
			return Optional.of("local delete user");
		}
		else
		{
			logger.warn("Permanent delete of {} unauthorized, not a local delete user", resourceTypeName);
			return Optional.empty();
		}
	}
}
