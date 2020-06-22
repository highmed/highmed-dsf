package org.highmed.dsf.fhir.prefer;

public enum PreferHandlingType
{
	STRICT("handling=strict"), LENIENT("handling=lenient");

	private final String headerValue;

	private PreferHandlingType(String headerValue)
	{
		this.headerValue = headerValue;
	}

	public static PreferHandlingType fromString(String prefer)
	{
		if (prefer == null)
			return LENIENT;

		switch (prefer)
		{
			case "handling=strict":
				return STRICT;
			case "handling=lenient":
			default:
				return LENIENT;
		}
	}

	public String getHeaderValue()
	{
		return headerValue;
	}
}
