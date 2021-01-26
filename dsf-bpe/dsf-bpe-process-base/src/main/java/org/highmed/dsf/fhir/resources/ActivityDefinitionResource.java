package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.MetadataResource;

public class ActivityDefinitionResource extends AbstractResource
{
	private ActivityDefinitionResource(Class<? extends MetadataResource> type, String dependecyJarName, String url,
			String version, String name, String fileName)
	{
		super(type, dependecyJarName, url, version, name, fileName);
	}

	public static ActivityDefinitionResource file(String fileName)
	{
		return new ActivityDefinitionResource(ActivityDefinition.class, null, null, null, null,
				Objects.requireNonNull(fileName, "fileName"));
	}

	public static ActivityDefinitionResource dependency(String dependecyJarName, String url, String version)
	{
		return new ActivityDefinitionResource(ActivityDefinition.class,
				Objects.requireNonNull(dependecyJarName, "dependecyJarName"), Objects.requireNonNull(url, "url"),
				Objects.requireNonNull(version, "version"), null, null);
	}
}
