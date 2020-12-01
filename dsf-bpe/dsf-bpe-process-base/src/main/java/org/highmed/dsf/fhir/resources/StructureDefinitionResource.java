package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.StructureDefinition;

public class StructureDefinitionResource extends AbstractResource
{
	private StructureDefinitionResource(Class<? extends MetadataResource> type, String dependecyNameAndVersion,
			String structureDefinitionUrl, String structureDefinitionVersion, String structureDefinitionFileName)
	{
		super(type, dependecyNameAndVersion, structureDefinitionUrl, structureDefinitionVersion, null,
				structureDefinitionFileName);
	}

	public static StructureDefinitionResource file(String structureDefinitionFileName)
	{
		return new StructureDefinitionResource(StructureDefinition.class, null, null, null,
				Objects.requireNonNull(structureDefinitionFileName, "structureDefinitionFileName"));
	}

	public static StructureDefinitionResource dependency(String dependencyNameAndVersion, String structureDefinitionUrl,
			String structureDefinitionVersion)
	{
		return new StructureDefinitionResource(StructureDefinition.class,
				Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(structureDefinitionUrl, "structureDefinitionUrl"),
				Objects.requireNonNull(structureDefinitionVersion, "structureDefinitionVersion"), null);
	}
}
