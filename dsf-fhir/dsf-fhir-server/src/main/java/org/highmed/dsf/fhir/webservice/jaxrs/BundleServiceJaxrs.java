package org.highmed.dsf.fhir.webservice.jaxrs;

import javax.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.BundleService;
import org.hl7.fhir.r4.model.Bundle;

@Path(BundleServiceJaxrs.PATH)
public class BundleServiceJaxrs extends AbstractResourceServiceJaxrs<Bundle, BundleService> implements BundleService
{
	public static final String PATH = "Bundle";

	public BundleServiceJaxrs(BundleService delegate)
	{
		super(delegate);
	}
}
