package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;

public class NamingSystemResource extends AbstractResource
{
	private NamingSystemResource(Class<? extends MetadataResource> type, String dependencyNameAndVersion,
			String namingSystemName, String namingSystemFileName)
	{
		super(type, dependencyNameAndVersion, null, null, namingSystemName, namingSystemFileName);
	}

	public static NamingSystemResource file(String namingSystemFileName)
	{
		return new NamingSystemResource(NamingSystem.class, null, null, Objects.requireNonNull(namingSystemFileName, "namingSystemFileName"));
	}

	public static NamingSystemResource dependency(String dependencyNameAndVersion, String namingSystemName)
	{
		return new NamingSystemResource(NamingSystem.class,
				Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(namingSystemName, "namingSystemName"), null);
	}
}
