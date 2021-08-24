package org.highmed.dsf.fhir.authentication;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

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
		return (User) httpRequest.get().getSession().getAttribute(AuthenticationFilter.USER_PROPERTY);
	}

	/**
	 * @param expectedRoles
	 *            not <code>null</code>
	 * @throws WebApplicationException
	 *             with status {@link Status#UNAUTHORIZED} if there is no current user, with status
	 *             {@link Status#FORBIDDEN} if the current user does not have one of the provided role
	 */
	public void checkCurrentUserHasOneOfRoles(UserRole... expectedRoles)
	{
		User user = getCurrentUser();

		if (user == null)
		{
			logger.warn("Current user is null, sending {}", Status.UNAUTHORIZED);
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
		else if (!UserRole.userHasOneOfRoles(user, expectedRoles))
		{
			logger.warn("Current user {} has non of roles {}, sending {}", user.getName(),
					Arrays.stream(expectedRoles).map(r -> r.name()).collect(Collectors.joining(", ", "{", "}")),
					Status.FORBIDDEN);

			throw new WebApplicationException(Status.FORBIDDEN);
		}
	}
}
