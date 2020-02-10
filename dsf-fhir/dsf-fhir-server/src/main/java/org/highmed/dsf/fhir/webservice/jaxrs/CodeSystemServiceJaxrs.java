package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.CodeSystemService;
import org.hl7.fhir.r4.model.CodeSystem;

@Path(CodeSystemServiceJaxrs.PATH)
public class CodeSystemServiceJaxrs extends AbstractResourceServiceJaxrs<CodeSystem, CodeSystemService>
		implements CodeSystemService
{
	public static final String PATH = "CodeSystem";

	public CodeSystemServiceJaxrs(CodeSystemService delegate)
	{
		super(delegate);
	}
}
