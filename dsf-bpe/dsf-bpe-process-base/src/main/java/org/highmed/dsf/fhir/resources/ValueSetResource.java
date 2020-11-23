package org.highmed.dsf.fhir.resources;

import java.util.Objects;

import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetResource extends AbstractResource
{
	private ValueSetResource(Class<? extends MetadataResource> type, String dependencyNameAndVersion,
			String valueSetUrl, String valueSetVersion, String valueSetFileName)
	{
		super(type, dependencyNameAndVersion, valueSetUrl, valueSetVersion, null, valueSetFileName);
	}

	public static ValueSetResource file(String valueSetFileName)
	{
		return new ValueSetResource(ValueSet.class, null, null, null,
				Objects.requireNonNull(valueSetFileName, "valueSetFileName"));
	}

	public static ValueSetResource dependency(String dependencyNameAndVersion, String valueSetUrl,
			String valueSetVersion)
	{
		return new ValueSetResource(ValueSet.class,
				Objects.requireNonNull(dependencyNameAndVersion, "dependencyNameAndVersion"),
				Objects.requireNonNull(valueSetUrl, "valueSetUrl"),
				Objects.requireNonNull(valueSetVersion, "valueSetVersion"), null);
	}
}
