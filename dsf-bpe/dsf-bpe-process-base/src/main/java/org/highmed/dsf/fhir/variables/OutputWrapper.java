package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO: check if Serializable can be replaced by JSON serialization
public class OutputWrapper implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String system;
	private final List<Pair<String, String>> keyValueList = new ArrayList<>();

	public OutputWrapper(String system, Collection<? extends Pair<String, String>> keyValueList)
	{
		this.system = system;
		if (keyValueList != null)
			this.keyValueList.addAll(keyValueList);
	}

	public String getSystem()
	{
		return system;
	}

	public List<Pair<String, String>> getKeyValueList()
	{
		return Collections.unmodifiableList(keyValueList);
	}
}
