package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OutputWrapper implements Serializable
{
	private final String system;
	private final List<SimpleEntry<String, String>> keyValueList;

	public OutputWrapper(String system)
	{
		this.system = system;
		this.keyValueList = new ArrayList<>();
	}

	public String getSystem()
	{
		return system;
	}

	public Collection<SimpleEntry<String, String>> getKeyValueMap()
	{
		return keyValueList;
	}

	public void addKeyValue(String key, String value)
	{
		keyValueList.add(new SimpleEntry<>(key, value));
	}
}
