package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.NamingSystem;

public class NamingSystemResource extends AbstractResource
{
	private NamingSystemResource(Class<? extends MetadataResource> type, String dependecyJarName, String url,
			String version, String name, String fileName)
	{
		super(type, dependecyJarName, url, version, name, fileName);
	}

	public static NamingSystemResource file(String fileName)
	{
		return new NamingSystemResource(NamingSystem.class, null, null, null, null,
				Objects.requireNonNull(fileName, "fileName"));
	}

	public static NamingSystemResource dependency(String dependecyJarName, String name)
	{
		return new NamingSystemResource(NamingSystem.class,
				Objects.requireNonNull(dependecyJarName, "dependecyJarName"), null, null,
				Objects.requireNonNull(name, "name"), null);
	}
}
