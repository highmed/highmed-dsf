package org.highmed.dsf.fhir.authorization;

import java.util.Objects;

import org.highmed.dsf.fhir.authentication.User;
import org.highmed.dsf.fhir.authentication.UserRole;
import org.highmed.dsf.fhir.dao.ResourceDao;
import org.highmed.dsf.fhir.search.SearchQueryUserFilter;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractAuthorizationRule<R extends Resource, D extends ResourceDao<R>>
		implements AuthorizationRule<R>, InitializingBean
{
	protected final D dao;

	public AbstractAuthorizationRule(D dao)
	{
		this.dao = dao;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dao, "dao");
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
}
