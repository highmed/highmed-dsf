package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;


public class OutputWrapper implements Serializable
{
	private String system;
	private final Collection<SimpleEntry<String, String>> keyValueList;

	public OutputWrapper(String system)
	{
		this.system = system;
		this.keyValueList = new ArrayList<>();
	}

	public void addKeyValue(String key, String value)
	{
		keyValueList.add(new SimpleEntry<>(key, value));
	}

	public String getSystem()
	{
		return system;
	}

	public void setSystem(String system)
	{
		this.system = system;
	}

	public Collection<SimpleEntry<String, String>> getKeyValueMap()
	{
		return keyValueList;
	}
}
