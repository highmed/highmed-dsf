package org.highmed.dsf.fhir.webservice.secure;

import org.highmed.dsf.fhir.help.ResponseGenerator;
import org.highmed.dsf.fhir.webservice.specification.ValueSetService;
import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetServiceSecure extends AbstractServiceSecure<ValueSet, ValueSetService> implements ValueSetService
{
	public ValueSetServiceSecure(ValueSetService delegate, ResponseGenerator responseGenerator)
	{
		super(delegate, responseGenerator);
	}
}
