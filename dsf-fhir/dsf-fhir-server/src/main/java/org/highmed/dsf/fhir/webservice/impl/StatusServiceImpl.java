package org.highmed.dsf.fhir.webservice.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.dbcp2.BasicDataSource;
import org.highmed.dsf.fhir.webservice.specification.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class StatusServiceImpl implements StatusService, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

	private final String path;
	private final BasicDataSource dataSource;

	public StatusServiceImpl(String path, BasicDataSource dataSource)
	{
		this.path = path;
		this.dataSource = dataSource;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(dataSource, "dataSource");
	}

	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public Response status(UriInfo uri, HttpHeaders headers, HttpServletRequest httpServletRequest)
	{
		try (Connection connection = dataSource.getConnection())
		{
			return Response.ok().build();
		}
		catch (SQLException e)
		{
			logger.error("Error while accessing DB", e);
			return Response.serverError().build();
		}
	}
}
