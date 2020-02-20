package org.highmed.dsf.fhir.authorization;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.dao.provider.DaoProvider;
import org.highmed.dsf.fhir.search.SearchQueryUserFilter;
import org.highmed.dsf.fhir.service.ReferenceResolver;
import org.highmed.dsf.fhir.service.ResourceReference;
import org.highmed.dsf.fhir.service.ResourceReference.ReferenceType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractAuthorizationRule<R extends Resource, D extends ResourceDao<R>>
		implements AuthorizationRule<R>, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractAuthorizationRule.class);

	protected final Class<R> resourceType;
	protected final DaoProvider daoProvider;
	protected final String serverBase;
	protected final ReferenceResolver referenceResolver;

	public AbstractAuthorizationRule(Class<R> resourceType, DaoProvider daoProvider, String serverBase,
			ReferenceResolver referenceResolver)
	{
		this.resourceType = resourceType;
		this.daoProvider = daoProvider;
		this.serverBase = serverBase;
		this.referenceResolver = referenceResolver;
	}

	@SuppressWarnings("unchecked")
	protected final D getDao()
	{
		return (D) daoProvider.getDao(resourceType).orElseThrow();
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(resourceType, "resourceType");
		Objects.requireNonNull(daoProvider, "daoProvider");
	}

	protected final boolean isLocalUser(User user)
	{
		return user != null && UserRole.LOCAL.equals(user.getRole());
	}

	protected final boolean isRemoteUser(User user)
	{
		return user != null && UserRole.REMOTE.equals(user.getRole());
	}

	protected final boolean hasLocalOrRemoteAuthorizationRole(Resource resource)
	{
		return resource.hasMeta() && resource.getMeta().getTag().stream()
				.anyMatch(c -> SearchQueryUserFilter.AUTHORIZATION_ROLE_SYSTEM.equals(c.getSystem())
						&& (SearchQueryUserFilter.AUTHORIZATION_ROLE_VALUE_LOCAL.equals(c.getCode())
								|| SearchQueryUserFilter.AUTHORIZATION_ROLE_VALUE_REMOTE.equals(c.getCode())));
	}

	protected final boolean hasLocalAuthorizationRole(Resource resource)
	{
		return hasAuthorizationRole(resource, SearchQueryUserFilter.AUTHORIZATION_ROLE_VALUE_LOCAL);
	}

	protected final boolean hasRemoteAuthorizationRole(Resource resource)
	{
		return hasAuthorizationRole(resource, SearchQueryUserFilter.AUTHORIZATION_ROLE_VALUE_REMOTE);
	}

	private boolean hasAuthorizationRole(Resource resource, String role)
	{
		return resource.hasMeta() && resource.getMeta().getTag().stream().anyMatch(
				c -> SearchQueryUserFilter.AUTHORIZATION_ROLE_SYSTEM.equals(c.getSystem()) && role.equals(c.getCode()));
	}

	protected final boolean isCurrentUserPartOfReferencedOrganizations(User user, String referenceLocation,
			Collection<? extends Reference> references)
	{
		return references.stream().anyMatch(r -> isCurrentUserPartOfReferencedOrganization(user, referenceLocation, r));
	}

	protected final boolean isCurrentUserPartOfReferencedOrganization(User user, String referenceLocation,
			Reference reference)
	{
		if (reference == null)
		{
			logger.warn("Null reference while checking if user part of referenced organization");
			return false;
		}
		else
		{
			ResourceReference resReference = new ResourceReference(referenceLocation, reference, Organization.class);

			if (!EnumSet.of(ReferenceType.LITERAL_INTERNAL, ReferenceType.LOGICAL)
					.contains(resReference.getType(serverBase)))
			{
				logger.warn("Reference of type {} not supported while checking if user part of referenced organization",
						resReference.getType(serverBase));
				return false;
			}

			Optional<Resource> resource = referenceResolver.resolveReference(user, resReference);
			if (resource.isPresent() && resource.get() instanceof Organization)
			{
				boolean sameOrganization = user.getOrganization().getIdElement().equals(resource.get().getIdElement());
				if (!sameOrganization)
					logger.warn(
							"Current user not part organization {} while checking if user part of referenced organization",
							resource.get().getIdElement().getValue());

				return sameOrganization;
			}
			else
			{
				logger.warn(
						"Reference to organization could not be resolved while checking if user part of referenced organization");
				return false;
			}
		}
	}

}
