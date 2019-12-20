package org.highmed.dsf.fhir.variables;

public class Output
{
	private final String system;
	private final String code;
	private final String value;
	private final String extensionUrl;
	private final String extensionValue;

	private boolean hasExtension;

	public Output(String system, String code, String value)
	{
		this(system, code, value, null, null);
		hasExtension = false;
	}

	public Output(String system, String code, String value, String extensionUrl, String extensionValue)
	{
		this.system = system;
		this.code = code;
		this.value = value;
		this.extensionUrl = extensionUrl;
		this.extensionValue = extensionValue;
		this.hasExtension = true;
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

	public String getExtensionUrl()
	{
		return extensionUrl;
	}

	public String getExtensionValue()
	{
		return extensionValue;
	}

	public boolean hasExtension()
	{
		return hasExtension;
	}
}
