package org.highmed.dsf.fhir.dao.exception;

public class ResourceVersionNoMatchException extends Exception
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final long expectedVersion;
	private final long latestVersion;

	public ResourceVersionNoMatchException(String id, long expectedVersion, long latestVersion)
	{
		this.id = id;
		this.expectedVersion = expectedVersion;
		this.latestVersion = latestVersion;
	}

	public String getId()
	{
		return id;
	}

	public long getExpectedVersion()
	{
		return expectedVersion;
	}

	public long getLatestVersion()
	{
		return latestVersion;
	}
}
