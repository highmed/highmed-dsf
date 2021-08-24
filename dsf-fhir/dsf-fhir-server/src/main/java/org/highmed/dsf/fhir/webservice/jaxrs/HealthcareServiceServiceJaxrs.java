package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.HealthcareServiceService;
import org.hl7.fhir.r4.model.HealthcareService;

@Path(HealthcareServiceServiceJaxrs.PATH)
public class HealthcareServiceServiceJaxrs extends
		AbstractResourceServiceJaxrs<HealthcareService, HealthcareServiceService> implements HealthcareServiceService
{
	public static final String PATH = "HealthcareService";

	public HealthcareServiceServiceJaxrs(HealthcareServiceService delegate)
	{
		super(delegate);
	}
}
