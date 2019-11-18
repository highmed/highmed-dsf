package org.highmed.dsf.fhir.variables;

public class Output
{
	private final String system;
	private final String code;
	private final String value;

	public Output(String system, String code, String value)
	{
		this.system = system;
		this.code = code;
		this.value = value;
	}

	public String getSystem()
	{
		return system;
	}

	public String getCode()
	{
		return code;
	}

	public String getValue()
	{
		return value;
	}
}
