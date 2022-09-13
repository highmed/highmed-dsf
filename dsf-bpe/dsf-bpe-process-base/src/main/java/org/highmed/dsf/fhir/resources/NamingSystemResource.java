package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.NamingSystem;

public class NamingSystemResource extends AbstractResource
{
	private NamingSystemResource(String dependencyNameAndVersion, String namingSystemName, String namingSystemFileName)
	{
		super(NamingSystem.class, dependencyNameAndVersion, null, null, namingSystemName, namingSystemFileName);
	}

	public static NamingSystemResource file(String namingSystemFileName)
	{
		return new NamingSystemResource(null, null,
				Objects.requireNonNull(namingSystemFileName, "namingSystemFileName"));
	}

	public static NamingSystemResource dependency(String dependencyNameAndVersion, String namingSystemName)
	{
		return new NamingSystemResource(Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(namingSystemName, "namingSystemName"), null);
	}
}
