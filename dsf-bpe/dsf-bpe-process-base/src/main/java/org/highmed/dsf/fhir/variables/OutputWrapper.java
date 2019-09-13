package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OutputWrapper implements Serializable
{
	private String system;
	private Map<String, String> keyValueMap;

	public OutputWrapper(String system)
	{
		this.system = system;
		this.keyValueMap = new HashMap<>();
	}

	public OutputWrapper(String system, Map<String, String> keyValueMap)
	{
		this.system = system;
		this.keyValueMap = keyValueMap;
	}

	public void addKeyValue(String key, String value)
	{
		keyValueMap.put(key, value);
	}

	public String getSystem()
	{
		return system;
	}

	public void setSystem(String system)
	{
		this.system = system;
	}

	public Map<String, String> getKeyValueMap()
	{
		return keyValueMap;
	}
}
