package org.highmed.dsf.bpe.webservice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@Path(StatusService.PATH)
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class StatusService implements InitializingBean
{
	public static final String PATH = "status";
	public static final int PORT = 10002;

	private static final Logger logger = LoggerFactory.getLogger(StatusService.class);

	private final BasicDataSource dataSource;

	public StatusService(BasicDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(dataSource, "dataSource");
	}

	@GET
	public Response status(@Context UriInfo uri, @Context HttpHeaders headers, @Context HttpServletRequest request)
	{
		logger.trace("GET {}, Local port {}", uri.getRequestUri().toString(), request.getLocalPort());

		if (request.getLocalPort() != PORT)
		{
			logger.warn("Sending '401 Unauthorized' request not on status port {}", PORT);
			return Response.status(Status.UNAUTHORIZED).build();
		}

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
