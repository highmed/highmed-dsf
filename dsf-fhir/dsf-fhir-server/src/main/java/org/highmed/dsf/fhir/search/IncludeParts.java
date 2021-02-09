package org.highmed.dsf.fhir.search;

public class IncludeParts
{
	private final String sourceResourceTypeName;
	private final String searchParameterName;
	private final String targetResourceTypeName;

	public IncludeParts(String sourceResourceTypeName, String searchParameterName, String targetResourceTypeName)
	{
		this.sourceResourceTypeName = sourceResourceTypeName;
		this.searchParameterName = searchParameterName;
		this.targetResourceTypeName = targetResourceTypeName;
	}

	public static IncludeParts fromString(String includeParameterValue)
	{
		if (includeParameterValue == null || includeParameterValue.isBlank())
			return new IncludeParts(null, null, null);
		else
		{
			String[] parts = includeParameterValue.split(":");

			String sourceResourceTypeName = null, searchParameterName = null, targetResourceTypeName = null;
			if (parts.length > 0)
				sourceResourceTypeName = parts[0];
			if (parts.length > 1)
				searchParameterName = parts[1];
			if (parts.length > 2)
				targetResourceTypeName = parts[2];

			return new IncludeParts(sourceResourceTypeName, searchParameterName, targetResourceTypeName);
		}
	}

	public String toBundleUriQueryParameterValue()
	{
		return getSourceResourceTypeName() + ":" + getSearchParameterName()
				+ (getTargetResourceTypeName() != null ? (":" + getTargetResourceTypeName()) : "");
	}

	public String getSourceResourceTypeName()
	{
		return sourceResourceTypeName;
	}

	public String getSearchParameterName()
	{
		return searchParameterName;
	}

	public String getTargetResourceTypeName()
	{
		return targetResourceTypeName;
	}

	public boolean matches(String resourceTypeName, String parameterName, String targetResourceTypeName)
	{
		return resourceTypeName.equals(getSourceResourceTypeName()) && parameterName.equals(getSearchParameterName())
				&& (getTargetResourceTypeName() == null || targetResourceTypeName.equals(getTargetResourceTypeName()));
	}

	@Override
	public String toString()
	{
		if (searchParameterName == null && targetResourceTypeName == null)
			return sourceResourceTypeName;
		else if (targetResourceTypeName == null)
			return sourceResourceTypeName + ":" + searchParameterName;
		else
			return sourceResourceTypeName + ":" + searchParameterName + ":" + targetResourceTypeName;
	}
}