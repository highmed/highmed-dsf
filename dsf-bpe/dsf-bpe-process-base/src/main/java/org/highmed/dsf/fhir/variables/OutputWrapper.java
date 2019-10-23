package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.List;

public class OutputWrapper implements Serializable
{
	private final String system;
	private final List<Pair<String, String>> keyValueList;

	public OutputWrapper(String system, List<Pair<String, String>> keyValueList)
	{
		this.system = system;
		this.keyValueList = keyValueList;
	}

	public String getSystem()
	{
		return system;
	}

	public List<Pair<String, String>> getKeyValueList()
	{
		return keyValueList;
	}
}
