package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.ProvenanceService;
import org.hl7.fhir.r4.model.Provenance;

public class ProvenanceServiceSecure extends AbstractServiceSecure<Provenance, ProvenanceService>
		implements ProvenanceService
{
	public ProvenanceServiceSecure(ProvenanceService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
