package org.highmed.dsf.fhir.cors;

import java.io.IOException;
import java.util.Objects;

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

public class CorsFilter implements Filter
{
	private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

	private CorsFilterConfig corsFilterConfig;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
		logger.debug("Init {}", CorsFilter.class.getName());

		corsFilterConfig = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext())
				.getBean(CorsFilterConfig.class);

		Objects.requireNonNull(corsFilterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		String origin = httpServletRequest.getHeader("Origin");
		logger.debug("doFilter for origin {} ...", origin);

		if (corsFilterConfig.originAllowed(origin))
		{
			httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
			httpServletResponse.addHeader("Access-Control-Allow-Origin", origin);
			httpServletResponse.addHeader("Access-Control-Allow-Headers",
					"authorization, content-type, x-http-method-override");
			httpServletResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		}

		if ("OPTIONS".equals(httpServletRequest.getMethod()))
			httpServletResponse.sendError(Status.NO_CONTENT.getStatusCode());
		else
			chain.doFilter(request, response);
	}

	@Override
	public void destroy()
	{
	}
}
