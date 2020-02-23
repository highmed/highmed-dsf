package org.highmed.dsf.fhir.authorization;

import java.util.EnumSet;
import java.util.Optional;

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

	public BinaryAuthorizationRule(DaoProvider daoProvider, String serverBase, ReferenceResolver referenceResolver)
	{
		super(Binary.class, daoProvider, serverBase, referenceResolver);
	}

	@Override
	public Optional<String> reasonCreateAllowed(User user, Binary newResource)
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
					Optional<Resource> securityContext = referenceResolver.resolveReference(user, reference);
					if (securityContext.isPresent())
					{
						logger.info("Create of Binary authorized for local user '{}', Binary.SecurityContext resolved",
								user.getName());
						return Optional.of("local user, Binary.SecurityContext resolved");
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
	public Optional<String> reasonReadAllowed(User user, Binary existingResource)
	{
		ResourceReference reference = new ResourceReference("Binary.SecurityContext",
				existingResource.getSecurityContext(), Organization.class);
		if (EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL).contains(reference.getType(serverBase)))
		{
			if (isCurrentUserPartOfReferencedOrganization(user, "Binary.SecurityContext",
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
		else
		{
			logger.warn("Read of Binary unauthorized, securityContext not a literal internal or logical reference");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonUpdateAllowed(User user, Binary oldResource, Binary newResource)
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
					Optional<Resource> oldSecurityContext = referenceResolver.resolveReference(user, oldReference);
					Optional<Resource> newSecurityContext = referenceResolver.resolveReference(user, newReference);
					if (oldSecurityContext.isPresent() && newSecurityContext.isPresent())
					{
						logger.info(
								"Update of Binary authorized for local user '{}', Binary.SecurityContext could be resolved (old and new)",
								user.getName());
						return Optional.of("local user, Binary.SecurityContext resolved");
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
	public Optional<String> reasonDeleteAllowed(User user, Binary oldResource)
	{
		if (isLocalUser(user))
		{
			if (oldResource.hasData() && oldResource.hasContentType() && oldResource.hasSecurityContext())
			{
				ResourceReference reference = new ResourceReference("Binary.SecurityContext",
						oldResource.getSecurityContext(), Organization.class);
				if (EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL)
						.contains(reference.getType(serverBase)))
				{
					Optional<Resource> securityContext = referenceResolver.resolveReference(user, reference);
					if (securityContext.isPresent())
					{
						logger.info("Delete of Binary authorized for local user '{}', Binary.SecurityContext resolved",
								user.getName());
						return Optional.of("local user, Binary.SecurityContext resolved");
					}
					else
					{
						logger.warn("Delete of Binary unauthorized, securityContext reference could not be resolved");
						return Optional.empty();
					}
				}
				else
				{
					logger.warn(
							"Delete of Binary unauthorized, securityContext not a literal internal or logical reference");
					return Optional.empty();
				}
			}
			else
			{
				logger.warn("Delete of Binary unauthorized, missing data or contentType or securityContext");
				return Optional.empty();
			}
		}
		else
		{
			logger.warn("Delete of Binary unauthorized, not a local user");
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> reasonSearchAllowed(User user)
	{
		if (isLocalUser(user))
		{
			logger.info("Search of Binary authorized for {} user '{}'", user.getRole(), user.getName());
			return Optional.of("Allowed for local users");
		}
		else
		{
			logger.warn("Search of Binary unauthorized, not a local user");
			return Optional.empty();
		}
	}
}
