package org.highmed.dsf.fhir.cors;

import java.io.IOException;

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

public class CorsFilter implements Filter
{
	private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
		logger.debug("Init {}", CorsFilter.class.getName());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		logger.debug("{} doFilter '{}' ...", CorsFilter.class.getName(), httpServletRequest.getPathInfo());

		httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
		httpServletResponse.addHeader("Access-Control-Allow-Origin", getHostAndPort(httpServletRequest));
		httpServletResponse.addHeader("Access-Control-Allow-Headers",
				"authorization, content-type, x-http-method-override");
		httpServletResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");

		if ("OPTIONS".equals(httpServletRequest.getMethod()))
			httpServletResponse.sendError(Status.NO_CONTENT.getStatusCode());
		else
			chain.doFilter(request, response);
	}

	/**
	 * Most unsecure implementation of CORS Filter possible :)
	 * Access-Control-Allow-Origin will always be the domain and port of the
	 * referer.
	 *
	 * @param httpServletRequest
	 * @return
	 */
	private String getHostAndPort(HttpServletRequest httpServletRequest)
	{
		String basis = httpServletRequest.getHeader("origin");

		if (basis == null)
			return "*";

		String[] parts = basis.split("/");
		return parts.length < 3 ? "*" : parts[0] + "//" + parts[1] + parts[2];
	}

	@Override
	public void destroy()
	{
	}
}
