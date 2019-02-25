package org.highmed.fhir.webservice.specification;

import javax.ws.rs.Path;

import org.hl7.fhir.r4.model.CodeSystem;

@Path(CodeSystemService.PATH)
public interface CodeSystemService extends BasicService<CodeSystem>
{
	final String PATH = "CodeSystem";
}
