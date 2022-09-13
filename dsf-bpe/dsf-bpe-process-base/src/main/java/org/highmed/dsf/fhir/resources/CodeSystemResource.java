package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.CodeSystem;

public class CodeSystemResource extends AbstractResource
{
	private CodeSystemResource(String dependencyNameAndVersion, String codeSystemUrl, String codeSystemVersion,
			String codeSystemFileName)
	{
		super(CodeSystem.class, dependencyNameAndVersion, codeSystemUrl, codeSystemVersion, null, codeSystemFileName);
	}

	public static CodeSystemResource file(String codeSystemFileName)
	{
		return new CodeSystemResource(null, null, null,
				Objects.requireNonNull(codeSystemFileName, "codeSystemFileName"));
	}

	public static CodeSystemResource dependency(String dependencyNameAndVersion, String codeSystemUrl,
			String codeSystemVersion)
	{
		return new CodeSystemResource(Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(codeSystemUrl, "codeSystemUrl"),
				Objects.requireNonNull(codeSystemVersion, "codeSystemVersion"), null);
	}
}
