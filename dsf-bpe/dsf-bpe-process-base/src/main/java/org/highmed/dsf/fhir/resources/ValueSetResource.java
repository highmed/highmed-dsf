package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetResource extends AbstractResource
{
	private ValueSetResource(Class<? extends MetadataResource> type, String dependecyJarName, String url,
			String version, String name, String fileName)
	{
		super(type, dependecyJarName, url, version, name, fileName);
	}

	public static ValueSetResource file(String fileName)
	{
		return new ValueSetResource(ValueSet.class, null, null, null, null,
				Objects.requireNonNull(fileName, "fileName"));
	}

	public static ValueSetResource dependency(String dependecyJarName, String url, String version)
	{
		return new ValueSetResource(ValueSet.class, Objects.requireNonNull(dependecyJarName, "dependecyJarName"),
				Objects.requireNonNull(url, "url"), Objects.requireNonNull(version, "version"), null, null);
	}
}
