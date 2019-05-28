package org.highmed.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.fhir.webservice.specification.BundleService;
import org.hl7.fhir.r4.model.Bundle;

@Path(BundleServiceJaxrs.PATH)
public class BundleServiceJaxrs extends AbstractServiceJaxrs<Bundle, BundleService>
		implements BundleService
{
	public static final String PATH = "Bundle";

	public BundleServiceJaxrs(BundleService delegate)
	{
		super(delegate);
	}
}
