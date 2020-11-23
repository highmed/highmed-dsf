package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.MetadataResource;

public class CodeSystemResource extends AbstractResource
{
	private CodeSystemResource(Class<? extends MetadataResource> type, String dependencyNameAndVersion,
			String codeSystemUrl, String codeSystemVersion, String codeSystemFileName)
	{
		super(type, dependencyNameAndVersion, codeSystemUrl, codeSystemVersion, null, codeSystemFileName);
	}

	public static CodeSystemResource file(String codeSystemFileName)
	{
		return new CodeSystemResource(CodeSystem.class, null, null, null,
				Objects.requireNonNull(codeSystemFileName, "codeSystemFileName"));
	}

	public static CodeSystemResource dependency(String dependencyNameAndVersion, String codeSystemUrl,
			String codeSystemVersion)
	{
		return new CodeSystemResource(CodeSystem.class,
				Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(codeSystemUrl, "codeSystemUrl"),
				Objects.requireNonNull(codeSystemVersion, "codeSystemVersion"), null);
	}
}
