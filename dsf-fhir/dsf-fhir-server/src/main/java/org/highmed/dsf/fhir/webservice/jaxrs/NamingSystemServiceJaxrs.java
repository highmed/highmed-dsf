package org.highmed.dsf.fhir.webservice.jaxrs;

import jakarta.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.NamingSystemService;
import org.hl7.fhir.r4.model.NamingSystem;

@Path(NamingSystemServiceJaxrs.PATH)
public class NamingSystemServiceJaxrs extends AbstractResourceServiceJaxrs<NamingSystem, NamingSystemService>
		implements NamingSystemService
{
	public static final String PATH = "NamingSystem";

	public NamingSystemServiceJaxrs(NamingSystemService delegate)
	{
		super(delegate);
	}
}
