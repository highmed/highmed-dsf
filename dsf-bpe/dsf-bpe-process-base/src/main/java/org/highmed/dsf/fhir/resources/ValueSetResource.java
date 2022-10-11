package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetResource extends AbstractResource
{
	private ValueSetResource(String dependencyNameAndVersion, String valueSetUrl, String valueSetVersion,
			String valueSetFileName)
	{
		super(ValueSet.class, dependencyNameAndVersion, valueSetUrl, valueSetVersion, null, valueSetFileName);
	}

	public static ValueSetResource file(String valueSetFileName)
	{
		return new ValueSetResource(null, null, null, Objects.requireNonNull(valueSetFileName, "valueSetFileName"));
	}

	public static ValueSetResource dependency(String dependencyNameAndVersion, String valueSetUrl,
			String valueSetVersion)
	{
		return new ValueSetResource(Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(valueSetUrl, "valueSetUrl"),
				Objects.requireNonNull(valueSetVersion, "valueSetVersion"), null);
	}
}
