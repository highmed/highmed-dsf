package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.PractitionerService;
import org.hl7.fhir.r4.model.Practitioner;

@Path(PractitionerServiceJaxrs.PATH)
public class PractitionerServiceJaxrs extends AbstractResourceServiceJaxrs<Practitioner, PractitionerService>
		implements PractitionerService
{
	public static final String PATH = "Practitioner";

	public PractitionerServiceJaxrs(PractitionerService delegate)
	{
		super(delegate);
	}
}
