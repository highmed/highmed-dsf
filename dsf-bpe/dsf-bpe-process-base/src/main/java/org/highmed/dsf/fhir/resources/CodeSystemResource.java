package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.MetadataResource;

public class CodeSystemResource extends AbstractResource
{
	private CodeSystemResource(Class<? extends MetadataResource> type, String dependecyJarName, String url,
			String version, String name, String fileName)
	{
		super(type, dependecyJarName, url, version, name, fileName);
	}

	public static CodeSystemResource file(String fileName)
	{
		return new CodeSystemResource(CodeSystem.class, null, null, null, null,
				Objects.requireNonNull(fileName, "fileName"));
	}

	public static CodeSystemResource dependency(String dependecyJarName, String url, String version)
	{
		return new CodeSystemResource(CodeSystem.class, Objects.requireNonNull(dependecyJarName, "dependecyJarName"),
				Objects.requireNonNull(url, "url"), Objects.requireNonNull(version, "version"), null, null);
	}
}
