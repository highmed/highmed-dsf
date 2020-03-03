package org.highmed.dsf.fhir.authorization;

import java.sql.Connection;
import java.util.EnumSet;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.OrganizationProvider;
import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.dao.BinaryDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryAuthorizationRule extends AbstractAuthorizationRule<Binary, BinaryDao>
{
	private static final Logger logger = LoggerFactory.getLogger(BinaryAuthorizationRule.class);

	public BinaryAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver,
			OrganizationProvider organizationProvider)
	{
		super(Binary.class, daoProvider, serverBase, referenceResolver, organizationProvider);
	}

	@Override
	public Optional<String> reasonCreateAllowed(Connection connection, User user, Binary newResource)
	{
		if (isLocalUser(user))
		{
			if (newResource.hasData() && newResource.hasContentType() && newResource.hasSecurityContext())
			{
				ResourceReference reference = new ResourceReference("Binary.SecurityContext",
						newResource.getSecurityContext(), Organization.class);
				if (EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL)
						.contains(reference.getType(serverBase)))
				{
					Optional<Resource> securityContext = referenceResolver.resolveReference(user, reference,
							connection);
					if (securityContext.isPresent())
					{
						if (securityContext.get() instanceof Organization)
						{
							logger.info(
									"Create of Binary authorized for local user '{}', Binary.SecurityContext resolved and instance of Organization",
									user.getName());
							return Optional.of("local user, Binary.SecurityContext(Organization) resolved");
						}
						else
						{
							logger.warn(
									"Create of Binary unauthorized, securityContext reference could be resolved but not instance of Organization");
							return Optional.empty();
						}
					}
					else
					{
						logger.warn("Create of Binary unauthorized, securityContext reference could not be resolved");
						return Optional.empty();
					}
				}
				else
				{
					logger.warn(
							"Create of Binary unauthorized, securityContext not a literal internal or logical reference");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Create of Binary unauthorized, missing data or contentType or securityContext");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Create of Binary unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonReadAllowed(Connection connection, User user, Binary existingResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Read of Binary authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else if (isCurrentUserPartOfReferencedOrganization(connection, user, "Binary.SecurityContext",
				existingResource.getSecurityContext()))
		{
			logger.info(
					"Read of Binary authorized, Binary.SecurityContext reference could be resolved and user '{}' is part of referenced organization",
					user.getName());
			return Optional.of("Binary.SecurityContext resolved and user part of referenced organization");
		}
		else
		{
			logger.warn(
					"Read of Binary unauthorized, securityContext reference could not be resolved or user '{}' not part of referenced organization",
					user.getName());
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(Connection connection, User user, Binary oldResource,
			Binary newResource)
	{
		if (isLocalUser(user))
		{
			if (newResource.hasData() && newResource.hasContentType() && newResource.hasSecurityContext())
			{
				ResourceReference oldReference = new ResourceReference("Binary.SecurityContext",
						oldResource.getSecurityContext(), Organization.class);
				ResourceReference newReference = new ResourceReference("Binary.SecurityContext",
						newResource.getSecurityContext(), Organization.class);
				if (EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL)
						.contains(oldReference.getType(serverBase))
						&& EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL)
								.contains(newReference.getType(serverBase)))
				{
					Optional<Resource> oldSecurityContext = referenceResolver.resolveReference(user, oldReference,
							connection);
					Optional<Resource> newSecurityContext = referenceResolver.resolveReference(user, newReference,
							connection);
					if (oldSecurityContext.isPresent() && newSecurityContext.isPresent())
					{
						if (oldSecurityContext.get() instanceof Organization
								&& newSecurityContext.get() instanceof Organization)
						{
							logger.info(
									"Create of Binary authorized for local user '{}', Binary.SecurityContext resolved and instance of Organization (old and new)",
									user.getName());
							return Optional.of("local user, Binary.SecurityContext(Organization) resolved");
						}
						else
						{
							logger.warn(
									"Create of Binary unauthorized, securityContext reference could be resolved (old and new) but not instance of Organization (old or new)");
							return Optional.empty();
						}
					}
					else
					{
						logger.warn(
								"Update of Binary unauthorized, securityContext reference could not be resolved (old or new)");
						return Optional.empty();
					}
				}
				else
				{
					logger.warn(
							"Update of Binary unauthorized, securityContext not a literal internal or logical reference (old or new)");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Update of Binary unauthorized, missing data or contentType or securityContext");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Update of Binary unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonDeleteAllowed(Connection connection, User user, Binary oldResource)
	{
		if (isLocalUser(user))
		{
			logger.info("Delete of Binary authorized for local user '{}'", user.getName());
			return Optional.of("local user");
		}
		else
		{
			logger.warn("Delete of Binary unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(Connection connection, User user)
	{
		logger.info("Search of Binary authorized for {} user '{}', will be fitered by users organization {}",
				user.getRole(), user.getName(), user.getOrganization().getIdElement().getValueAsString());
		return Optional.of("Allowed for all, filtered by users organization");
	}
}
