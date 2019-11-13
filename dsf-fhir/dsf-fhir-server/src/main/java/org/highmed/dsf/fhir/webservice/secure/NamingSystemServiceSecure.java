package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.NamingSystemService;
import org.hl7.fhir.r4.model.NamingSystem;

public class NamingSystemServiceSecure extends AbstractServiceSecure<NamingSystem, NamingSystemService>
		implements NamingSystemService
{
	public NamingSystemServiceSecure(NamingSystemService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
