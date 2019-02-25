package org.highmed.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.fhir.webservice.specification.HealthcareServiceService;
import org.hl7.fhir.r4.model.HealthcareService;

@Path(HealthcareServiceServiceJaxrs.PATH)
public class HealthcareServiceServiceJaxrs extends AbstractServiceJaxrs<HealthcareService, HealthcareServiceService>
		implements HealthcareServiceService
{
	public static final String PATH = "HealthcareService";

	public HealthcareServiceServiceJaxrs(HealthcareServiceService delegate)
	{
		super(delegate);
	}

	@Override
	public String getPath()
	{
		return PATH;
	}
}
