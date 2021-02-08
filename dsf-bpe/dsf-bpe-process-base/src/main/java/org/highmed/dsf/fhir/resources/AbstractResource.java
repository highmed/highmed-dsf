package org.highmed.dsf.fhir.resources;

import org.hl7.fhir.r4.model.MetadataResource;

public abstract class AbstractResource
{
	private final Class<? extends MetadataResource> type;

	private final String dependencyNameAndVersion;
	private final String url;
	private final String version;
	private final String name;

	private final String fileName;

	AbstractResource(Class<? extends MetadataResource> type, String dependencyNameAndVersion, String url,
			String version, String name, String fileName)
	{
		this.type = type;
		this.dependencyNameAndVersion = dependencyNameAndVersion;
		this.url = url;
		this.version = version;
		this.name = name;
		this.fileName = fileName;
	}

	public Class<? extends MetadataResource> getType()
	{
		return type;
	}

	public String getDependencyNameAndVersion()
	{
		return dependencyNameAndVersion;
	}

	public String getUrl()
	{
		return url;
	}

	public String getVersion()
	{
		return version;
	}

	public String getName()
	{
		return name;
	}

	public String getFileName()
	{
		return fileName;
	}

	public boolean isDependencyResource()
	{
		return dependencyNameAndVersion != null;
	}
}
