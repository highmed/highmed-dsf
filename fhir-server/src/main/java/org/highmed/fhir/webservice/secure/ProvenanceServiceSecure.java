package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.ProvenanceService;
import org.hl7.fhir.r4.model.Provenance;

public class ProvenanceServiceSecure extends AbstractServiceSecure<Provenance, ProvenanceService>
		implements ProvenanceService
{
	public ProvenanceServiceSecure(ProvenanceService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
