package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: check if Serializable can be replaced by JSON serialization
public class OutputWrapper implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String system;
	private final List<Pair<String, String>> keyValueList;

	public OutputWrapper(String system, Collection<? extends Pair<String, String>> keyValueList)
	{
		this(system, keyValueList == null ? null : keyValueList.stream());
	}

	public OutputWrapper(String system, Stream<? extends Pair<String, String>> keyValueList)
	{
		this.system = system;
		if (keyValueList != null)
			this.keyValueList = keyValueList.collect(Collectors.toList());
		else
			this.keyValueList = Collections.emptyList();
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
