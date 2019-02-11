package org.highmed.fhir.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path(TestService.PATH)
public class TestService
{
	public static final String PATH = "test";

	@GET
	public Response test()
	{
		return Response.ok("test").build();
	}
}
