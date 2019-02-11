package org.highmed.fhir.dao;

import org.hl7.fhir.r4.model.IdType;

public class ResourceDeletedException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	private final IdType id;

	public ResourceDeletedException(IdType id)
	{
		this.id = id;
	}
	
	public IdType getId()
	{
		return id;
	}
}
