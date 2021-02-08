package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.ValueSetService;
import org.hl7.fhir.r4.model.ValueSet;

@Path(ValueSetServiceJaxrs.PATH)
public class ValueSetServiceJaxrs extends AbstractResourceServiceJaxrs<ValueSet, ValueSetService>
		implements ValueSetService
{
	public static final String PATH = "ValueSet";

	public ValueSetServiceJaxrs(ValueSetService delegate)
	{
		super(delegate);
	}
}
