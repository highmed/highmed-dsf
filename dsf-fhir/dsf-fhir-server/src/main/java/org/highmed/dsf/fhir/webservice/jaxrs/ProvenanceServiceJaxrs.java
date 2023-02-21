package org.highmed.dsf.fhir.webservice.jaxrs;

import jakarta.ws.rs.Path;

import org.highmed.dsf.fhir.webservice.specification.ProvenanceService;
import org.hl7.fhir.r4.model.Provenance;

@Path(ProvenanceServiceJaxrs.PATH)
public class ProvenanceServiceJaxrs extends AbstractResourceServiceJaxrs<Provenance, ProvenanceService>
		implements ProvenanceService
{
	public static final String PATH = "Provenance";

	public ProvenanceServiceJaxrs(ProvenanceService delegate)
	{
		super(delegate);
	}
}
