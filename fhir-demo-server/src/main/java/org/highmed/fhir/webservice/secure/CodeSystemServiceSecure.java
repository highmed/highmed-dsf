package org.highmed.fhir.webservice.secure;

import org.highmed.fhir.help.ResponseGenerator;
import org.highmed.fhir.webservice.specification.CodeSystemService;
import org.hl7.fhir.r4.model.CodeSystem;

public class CodeSystemServiceSecure extends AbstractServiceSecure<CodeSystem, CodeSystemService>
		implements CodeSystemService
{
	public CodeSystemServiceSecure(CodeSystemService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
