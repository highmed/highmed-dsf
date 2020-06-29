package org.highmed.dsf.fhir.prefer;

public enum PreferReturnType
{
	MINIMAL("return=minimal"), REPRESENTATION("return=representation"), OPERATION_OUTCOME("return=OperationOutcome");

	private final String headerValue;

	private PreferReturnType(String headerValue)
	{
		this.headerValue = headerValue;
	}

	public static PreferReturnType fromString(String prefer)
	{
		if (prefer == null)
			return REPRESENTATION;

		switch (prefer)
		{
			case "return=minimal":
				return MINIMAL;
			case "return=OperationOutcome":
				return OPERATION_OUTCOME;
			case "return=representation":
			default:
				return REPRESENTATION;
		}
	}

	public String getHeaderValue()
	{
		return headerValue;
	}
}
