package org.highmed.dsf.fhir.history;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hl7.fhir.r4.model.Resource;

public class HistoryEntry
{
	private final UUID id;
	private final String version;
	private final String resourceType;
	private final String method;
	private final LocalDateTime lastUpdated;
	private final Resource resource;

	public HistoryEntry(UUID id, String version, String resourceType, String method, LocalDateTime lastUpdated,
			Resource resource)
	{
		this.id = id;
		this.version = version;
		this.resourceType = resourceType;
		this.method = method;
		this.lastUpdated = lastUpdated;
		this.resource = resource;
	}

	public UUID getId()
	{
		return id;
	}

	public String getVersion()
	{
		return version;
	}

	public String getResourceType()
	{
		return resourceType;
	}

	public String getMethod()
	{
		return method;
	}

	public LocalDateTime getLastUpdated()
	{
		return lastUpdated;
	}

	public Resource getResource()
	{
		return resource;
	}
}