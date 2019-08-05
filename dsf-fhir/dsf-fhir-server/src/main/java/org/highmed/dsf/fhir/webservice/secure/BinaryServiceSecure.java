package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.BinaryService;
import org.hl7.fhir.r4.model.Binary;

public class BinaryServiceSecure extends AbstractServiceSecure<Binary, BinaryService> implements BinaryService
{
	public BinaryServiceSecure(BinaryService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}