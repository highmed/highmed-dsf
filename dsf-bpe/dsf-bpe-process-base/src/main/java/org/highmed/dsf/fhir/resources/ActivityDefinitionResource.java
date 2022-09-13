package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.ActivityDefinition;

public class ActivityDefinitionResource extends AbstractResource
{
	private ActivityDefinitionResource(String dependecyJarName, String url, String version, String name,
			String fileName)
	{
		super(ActivityDefinition.class, dependecyJarName, url, version, name, fileName);
	}

	public static ActivityDefinitionResource file(String fileName)
	{
		return new ActivityDefinitionResource(null, null, null, null, Objects.requireNonNull(fileName, "fileName"));
	}

	public static ActivityDefinitionResource dependency(String dependecyJarName, String url, String version)
	{
		return new ActivityDefinitionResource(Objects.requireNonNull(dependecyJarName, "dependecyJarName"),
				Objects.requireNonNull(url, "url"), Objects.requireNonNull(version, "version"), null, null);
	}
}
