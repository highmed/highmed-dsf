package org.highmed.dsf.fhir.dao.exception;

import java.time.LocalDateTime;

import org.hl7.fhir.r4.model.IdType;

public class ResourceDeletedException extends Exception
{
	private static final long serialVersionUID = 1L;

	private final IdType id;
	private final LocalDateTime deleted;

	public ResourceDeletedException(IdType id, LocalDateTime deleted)
	{
		this.id = id;
		this.deleted = deleted;
	}

	public IdType getId()
	{
		return id;
	}

	public LocalDateTime getDeleted()
	{
		return deleted;
	}
}
