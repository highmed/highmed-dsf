package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.StructureDefinition;

public class StructureDefinitionResource extends AbstractResource
{
	private StructureDefinitionResource(String dependecyNameAndVersion, String structureDefinitionUrl,
			String structureDefinitionVersion, String structureDefinitionFileName)
	{
		super(StructureDefinition.class, dependecyNameAndVersion, structureDefinitionUrl, structureDefinitionVersion,
				null, structureDefinitionFileName);
	}

	public static StructureDefinitionResource file(String structureDefinitionFileName)
	{
		return new StructureDefinitionResource(null, null, null,
				Objects.requireNonNull(structureDefinitionFileName, "structureDefinitionFileName"));
	}

	public static StructureDefinitionResource dependency(String dependencyNameAndVersion, String structureDefinitionUrl,
			String structureDefinitionVersion)
	{
		return new StructureDefinitionResource(
				Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(structureDefinitionUrl, "structureDefinitionUrl"),
				Objects.requireNonNull(structureDefinitionVersion, "structureDefinitionVersion"), null);
	}
}
