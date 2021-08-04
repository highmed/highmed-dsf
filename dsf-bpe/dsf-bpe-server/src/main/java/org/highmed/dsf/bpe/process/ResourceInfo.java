package org.highmed.dsf.bpe.process;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class ResourceInfo implements Comparable<ResourceInfo>
{
	private final String resourceType;
	private final String url;
	private final String version;
	private final String name;

	private UUID resourceId;

	public ResourceInfo(String resourceType, String url, String version, String name)
	{
		this.resourceType = resourceType;
		this.url = url;
		this.version = version;
		this.name = name;

		validate();
	}

	private void validate()
	{
		Objects.requireNonNull(resourceType, "resourceType");

		if ("NamingSystem".equals(getResourceType()))
		{
			if (url != null)
				throw new IllegalArgumentException("url not null for " + resourceType);
			if (version != null)
				throw new IllegalArgumentException("version not null for " + resourceType);
			Objects.requireNonNull(name, "name");
		}
		else
		{
			Objects.requireNonNull(url, "url");
			Objects.requireNonNull(version, "version");
			if (name != null)
				throw new IllegalArgumentException("name not null for " + resourceType);
		}
	}

	public String getResourceType()
	{
		return resourceType;
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceType == null) ? 0 : resourceType.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceInfo other = (ResourceInfo) obj;
		if (resourceType == null)
		{
			if (other.resourceType != null)
				return false;
		}
		else if (!resourceType.equals(other.resourceType))
			return false;
		if (url == null)
		{
			if (other.url != null)
				return false;
		}
		else if (!url.equals(other.url))
			return false;
		if (version == null)
		{
			if (other.version != null)
				return false;
		}
		else if (!version.equals(other.version))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(ResourceInfo o)
	{
		Comparator<ResourceInfo> comparator;

		if ("NamingSystem".equals(getResourceType()))
			comparator = Comparator.comparing(ResourceInfo::getResourceType).thenComparing(ResourceInfo::getName);
		else
			comparator = Comparator.comparing(ResourceInfo::getResourceType).thenComparing(ResourceInfo::getUrl)
					.thenComparing(ResourceInfo::getVersion);

		return comparator.compare(this, o);
	}

	@Override
	public String toString()
	{
		return "ResouceInfo [resourceType=" + resourceType + ", url=" + url + ", version=" + version + ", name=" + name
				+ "]";
	}

	public String toConditionalUrl()
	{
		if ("NamingSystem".equals(getResourceType()))
			return "name=" + getName();
		else
			return "url=" + getUrl() + "&version=" + getVersion();
	}

	public UUID getResourceId()
	{
		return resourceId;
	}

	public ResourceInfo setResourceId(UUID resourceId)
	{
		this.resourceId = resourceId;

		return this;
	}

	public boolean hasResourceId()
	{
		return resourceId != null;
	}
}
