package org.highmed.dsf.fhir.authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationFilterConfigImpl implements AuthenticationFilterConfig
{
	public static AuthenticationFilterConfig createConfigForPathsRequiringAuthentication(String servletPath,
			NeedsAuthentication... services)
	{
		return createConfigForPathsRequiringAuthentication("", Arrays.asList(services));
	}

	public static AuthenticationFilterConfig createConfigForPathsRequiringAuthentication(String servletPath,
			List<NeedsAuthentication> services)
	{
		Objects.requireNonNull(servletPath, "servletPath");

		List<String> pathFromServices = services.stream().map(s ->
		{
			String path = s.getPath();
			return servletPath + (path.startsWith("/") ? path : "/" + path);

		}).collect(Collectors.toList());

		return new AuthenticationFilterConfigImpl(pathFromServices);
	}

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilterConfigImpl.class);

	private final List<String> pathsRequiringAuthentication;

	private AuthenticationFilterConfigImpl(List<String> pathsRequiringAuthentication)
	{
		this.pathsRequiringAuthentication = pathsRequiringAuthentication;

		logger.info("Paths requiring authentication: '{}'", pathsRequiringAuthentication);
	}

	public boolean needsAuthentication(HttpServletRequest request)
	{
		Objects.requireNonNull(request, "request");

		String path = request.getServletPath() + request.getPathInfo();

		boolean needsAuthentication = pathsRequiringAuthentication.stream().map(p -> path.startsWith(p)).filter(b -> b)
				.findAny().orElse(false);

		if (needsAuthentication)
			logger.debug("Request path: '{}{}' needs authentication", request.getServletPath(), request.getPathInfo());

		return needsAuthentication;
	}
}
