package org.highmed.dsf.fhir.authentication;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationFilterConfigImpl implements AuthenticationFilterConfig
{
	public static AuthenticationFilterConfig createConfigForPathsRequiringAuthentication(
			List<NeedsAuthentication> servicesRequiringAuthentication,
			List<DoesNotNeedAuthentication> servicesNotRequiringAuthentication)
	{
		List<String> pathsRequiringAuthentication = servicesRequiringAuthentication.stream().map(s ->
		{
			String path = s.getPath();
			return path.startsWith("/") ? path : "/" + path;

		}).collect(Collectors.toList());

		List<String> pathsNotRequiringAuthentication = servicesNotRequiringAuthentication.stream().map(s ->
		{
			String path = s.getPath();
			return path.startsWith("/") ? path : "/" + path;

		}).collect(Collectors.toList());

		return new AuthenticationFilterConfigImpl(pathsRequiringAuthentication, pathsNotRequiringAuthentication);
	}

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilterConfigImpl.class);

	private final List<String> pathsRequiringAuthentication;
	private final List<String> pathsNotRequiringAuthentication;

	private AuthenticationFilterConfigImpl(List<String> pathsRequiringAuthentication,
			List<String> pathsNotRequiringAuthentication)
	{
		this.pathsRequiringAuthentication = pathsRequiringAuthentication;
		this.pathsNotRequiringAuthentication = pathsNotRequiringAuthentication;

		logger.info("Paths requiring authentication: '{}'", pathsRequiringAuthentication);
		logger.info("Paths not requiring authentication: '{}'", pathsNotRequiringAuthentication);
	}

	public boolean needsAuthentication(HttpServletRequest request)
	{
		Objects.requireNonNull(request, "request");

		String path = request.getServletPath() + request.getPathInfo();

		if (pathsRequiringAuthentication.contains(path))
		{
			logger.debug("Request path: '{}' needs authentication", path);
			return true;
		}
		else if (pathsNotRequiringAuthentication.contains(path))
		{
			logger.debug("Request path: '{}' does not need authentication", path);
			return false;
		}
		else
		{
			logger.warn("Request path: '{}' not configured, sending 401 Unauthorized", path);
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}
}
