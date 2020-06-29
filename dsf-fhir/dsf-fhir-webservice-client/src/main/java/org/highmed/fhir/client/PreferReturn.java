package org.highmed.fhir.client;

import java.net.URI;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

public class PreferReturn
{
	private final IdType id;
	private final Resource resource;
	private final OperationOutcome operationOutcome;

	private PreferReturn(IdType id, Resource resource, OperationOutcome operationOutcome)
	{
		this.id = id;
		this.resource = resource;
		this.operationOutcome = operationOutcome;
	}

	public static PreferReturn minimal(URI location)
	{
		return new PreferReturn(new IdType(location.toString()), null, null);
	}

	public static PreferReturn resource(Resource resource)
	{
		return new PreferReturn(null, resource, null);
	}

	public static PreferReturn outcome(OperationOutcome operationOutcome)
	{
		return new PreferReturn(null, null, operationOutcome);
	}

	public IdType getId()
	{
		return id;
	}

	public Resource getResource()
	{
		return resource;
	}

	public OperationOutcome getOperationOutcome()
	{
		return operationOutcome;
	}
}
