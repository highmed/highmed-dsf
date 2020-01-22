package org.highmed.dsf.fhir.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Output
{
	private final String system;
	private final String code;
	private final String value;
	private final String extensionUrl;
	private final String extensionValue;

	private final boolean hasExtension;

	@JsonCreator
	public Output(
			@JsonProperty("system") String system,
			@JsonProperty("code") String code,
			@JsonProperty("value") String value)
	{
		this(system, code, value, null, null);
	}

	@JsonCreator
	public Output(
			@JsonProperty("system") String system,
			@JsonProperty("code") String code,
			@JsonProperty("value") String value,
			@JsonProperty("extensionUrl") String extensionUrl,
			@JsonProperty("extensionValue") String extensionValue)
	{
		this.system = system;
		this.code = code;
		this.value = value;
		this.extensionUrl = extensionUrl;
		this.extensionValue = extensionValue;

		this.hasExtension = extensionUrl != null;
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
