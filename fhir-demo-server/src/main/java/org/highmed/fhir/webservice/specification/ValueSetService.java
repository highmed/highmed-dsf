package org.highmed.fhir.webservice.specification;

import javax.ws.rs.Path;

import org.hl7.fhir.r4.model.ValueSet;

@Path(ValueSetService.PATH)
public interface ValueSetService extends BasicService<ValueSet>
{
	final String PATH = "ValueSet";
}
