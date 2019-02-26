package org.highmed.fhir.authentication;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProvider
{
	private static final Logger logger = LoggerFactory.getLogger(UserProvider.class);

	private final Supplier<HttpServletRequest> httpRequest;

	public UserProvider(Supplier<HttpServletRequest> httpRequest)
	{
		this.httpRequest = httpRequest;
	}

	public User getCurrentUser()
	{
		Object organization = httpRequest.get().getSession().getAttribute(AuthenticationFilter.ORGANIZATION_PROPERTY);
		return new User((Organization) organization);
	}

	public void checkCurrentUserHasOneOfRoles(UserRole... roles)
	{
		User user = getCurrentUser();

		if (user == null)
		{
			logger.warn("Current user is null, sending {}", Status.UNAUTHORIZED);
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
		else if (!userHasOneOfRoles(user, roles))
		{
			logger.warn("Current user {} has non of roles {}, sending {}", user.getName(),
					Arrays.stream(roles).map(r -> r.name()).collect(Collectors.joining(", ", "{", "}")),
					Status.FORBIDDEN);

			throw new WebApplicationException(Status.FORBIDDEN);
		}
	}

	private static boolean userHasOneOfRoles(User u, UserRole... roles)
	{
		for (UserRole role : roles)
			if (role.equals(u.getRole()))
				return true;

		return false;
	}
}
