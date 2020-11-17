package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.StructureDefinition;

public class StructureDefinitionResource extends AbstractResource
{
	private StructureDefinitionResource(Class<? extends MetadataResource> type, String dependecyJarName, String url,
			String version, String name, String fileName)
	{
		super(type, dependecyJarName, url, version, name, fileName);
	}

	public static StructureDefinitionResource file(String fileName)
	{
		return new StructureDefinitionResource(StructureDefinition.class, null, null, null, null,
				Objects.requireNonNull(fileName, "fileName"));
	}

	public static StructureDefinitionResource dependency(String dependecyJarName, String url, String version)
	{
		return new StructureDefinitionResource(StructureDefinition.class,
				Objects.requireNonNull(dependecyJarName, "dependecyJarName"), Objects.requireNonNull(url, "url"),
				Objects.requireNonNull(version, "version"), null, null);
	}
}
