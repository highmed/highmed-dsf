package org.highmed.dsf.fhir.authentication;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;

import javax.security.auth.x500.X500Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AuthenticationFilter implements Filter
{
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	public static final String USER_PROPERTY = AuthenticationFilter.class.getName() + ".user";

	private OrganizationProvider organizationProvider;
	private AuthenticationFilterConfig authenticationFilterConfig;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
		logger.debug("Init {}", AuthenticationFilter.class.getName());

		organizationProvider = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext())
				.getBean(OrganizationProvider.class);

		authenticationFilterConfig = WebApplicationContextUtils
				.getWebApplicationContext(filterConfig.getServletContext()).getBean(AuthenticationFilterConfig.class);

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(authenticationFilterConfig, "authenticationFilterConfig");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		if (!authenticationFilterConfig.needsAuthentication(httpServletRequest))
		{
			chain.doFilter(httpServletRequest, httpServletResponse);
		}
		else
		{
			Optional<User> user = getUser(httpServletRequest);
			if (user.isPresent())
			{
				logger.debug("User '{}' with role '{}' authenticated", user.get().getName(), user.get().getRole());
				httpServletRequest.getSession().setAttribute(USER_PROPERTY, user.get());

				chain.doFilter(httpServletRequest, httpServletResponse);
			}
			else
			{
				logger.warn("User '{}' not found, sending unauthorized",
						getCertificateDn(httpServletRequest).orElse("?"));
				httpServletResponse.sendError(Status.UNAUTHORIZED.getStatusCode());
			}
		}
	}

	private Optional<User> getUser(HttpServletRequest httpServletRequest)
	{
		X509Certificate[] certificates = (X509Certificate[]) httpServletRequest
				.getAttribute("javax.servlet.request.X509Certificate");

		if (certificates == null || certificates.length <= 0)
		{
			logger.warn("X509Certificate could not be retrieved");
			return Optional.empty();
		}
		else
			return organizationProvider.getOrganization(certificates[0]);
	}

	private Optional<String> getCertificateDn(HttpServletRequest httpServletRequest)
	{
		X509Certificate[] certificates = (X509Certificate[]) httpServletRequest
				.getAttribute("javax.servlet.request.X509Certificate");

		if (certificates == null || certificates.length <= 0)
			return Optional.empty();
		else
			return Optional.of(certificates[0].getSubjectX500Principal().getName(X500Principal.RFC1779));
	}

	@Override
	public void destroy()
	{
	}
}
