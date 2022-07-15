package org.highmed.dsf.fhir.authentication;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationFilterConfigImpl implements AuthenticationFilterConfig
{
	public static AuthenticationFilterConfig createConfigForPathsRequiringAuthentication(
			List<DoesNotNeedAuthentication> servicesNotRequiringAuthentication)
	{
		List<String> pathsNotRequiringAuthentication = servicesNotRequiringAuthentication.stream().map(s ->
		{
			String path = s.getPath();
			return path.startsWith("/") ? path : "/" + path;

		}).collect(Collectors.toList());

		return new AuthenticationFilterConfigImpl(pathsNotRequiringAuthentication);
	}

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilterConfigImpl.class);

	private final List<String> pathsNotRequiringAuthentication;

	private AuthenticationFilterConfigImpl(List<String> pathsNotRequiringAuthentication)
	{
		this.pathsNotRequiringAuthentication = pathsNotRequiringAuthentication;

		logger.info("Paths not requiring authentication: '{}'", pathsNotRequiringAuthentication);
	}

	public boolean needsAuthentication(HttpServletRequest request)
	{
		Objects.requireNonNull(request, "request");

		String path = request.getServletPath() + request.getPathInfo();

		if (pathsNotRequiringAuthentication.contains(path))
		{
			logger.trace("Request path: '{}' does not need authentication", path);
			return false;
		}
		else
		{
			logger.trace("Request path: '{}' needs authentication", path);
			return true;
		}
	}
}
